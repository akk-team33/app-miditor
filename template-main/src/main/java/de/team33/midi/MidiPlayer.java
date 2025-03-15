package de.team33.midi;

import de.team33.midi.util.ClassUtil;
import de.team33.midix.Timing;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;
import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import java.util.EnumSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

@SuppressWarnings("ClassWithTooManyMethods")
public class MidiPlayer extends Sender<MidiPlayer> {

    private static final Preferences PREFS = Preferences.userRoot().node(ClassUtil.getPathString(MidiPlayer.class));
    private static final Mode[] EMPTY_MODES = new Mode[0];

    private final Audience audience;
    private final Mapping mapping;
    private final MidiSequence sequence;
    private final Sequencer backing;
    private MidiDevice outputDevice;
    private Mode[] modes;

    public MidiPlayer(final MidiSequence sequence) throws MidiUnavailableException {
        super(MidiPlayer.class);
        this.audience = new Audience(new SimpleAsyncExecutor());
        this.mapping = Mapping.builder()
                              .put(Channel.SetPosition, () -> this)
                              .put(Channel.SetState, () -> this)
                              .put(Channel.SetTempo, () -> this)
                              .put(Channel.SetModes, () -> this)
                              .build();
        backing = MidiSystem.getSequencer(false);
        this.sequence = sequence;
        this.sequence.add(MidiSequence.Channel.SetTracks, this::onSetParts);
        audience.add(Channel.SetState, new STARTER());
    }

    private static MidiDevice newOutputDevice() throws MidiUnavailableException {
        final String preferedOutputDeviceName = PREFS.get("preferredOutputDeviceName", "");
        final Preferences prefs = PREFS.node("DeviceInfo");
        final MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        MidiDevice.Info preferedOutputDeviceInfo = null;

        for (int index = 0; index < infos.length; ++index) {
            final MidiDevice.Info info = infos[index];
            final Preferences node = prefs.node(String.format("%04d", index));
            node.put("name", info.getName());
            node.put("description", info.getDescription());
            node.put("vendor", info.getVendor());
            node.put("version", info.getVersion());
            if (preferedOutputDeviceName.equals(info.getName())) {
                preferedOutputDeviceInfo = info;
            }
        }

        final MidiDevice result;
        if (null == preferedOutputDeviceInfo) {
            result = MidiSystem.getSynthesizer();
        } else {
            result = MidiSystem.getMidiDevice(preferedOutputDeviceInfo);
        }

        PREFS.put("preferedOutputDeviceName", result.getDeviceInfo().getName());
        return result;
    }

    private void core_close(final Set<? super Channel> events) {
        if (backing.isOpen()) {
            outputDevice.close();
            outputDevice = null;
            backing.close();
            events.add(Channel.SetState);
        }
    }

    private void core_clrModes(final Set<? super Channel> events) {
        modes = null;
        events.add(Channel.SetModes);
    }

    private Mode[] core_Modes() {
        if (null == modes) {
            final javax.sound.midi.Sequence seq = backing.getSequence();
            if (null != seq) {
                final int length = seq.getTracks().length;
                int nNormal = 0;
                int iNormal = -1;
                modes = new Mode[length];

                for (int i = 0; i < length; ++i) {
                    if (backing.getTrackMute(i)) {
                        modes[i] = Mode.MUTE;
                    } else {
                        modes[i] = Mode.NORMAL;
                        ++nNormal;
                        iNormal = i;
                    }
                }

                if (1 == nNormal) {
                    modes[iNormal] = Mode.SOLO;
                }
            } else {
                modes = EMPTY_MODES;
            }
        }

        return modes;
    }

    private void core_open(final Set<? super Channel> events) {
        if (!backing.isOpen()) {
            try {
                outputDevice = newOutputDevice();
                outputDevice.open();
                backing.getTransmitter().setReceiver(outputDevice.getReceiver());
                backing.setSequence(sequence.backing());
                backing.open();
                events.add(Channel.SetState);
                events.add(Channel.SetTempo);
            } catch (final Exception e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }

    }

    private void core_setTickPosition(final long ticks, final Set<? super Channel> events) {
        if (!backing.isOpen()) {
            core_open(events);
        }

        backing.setTickPosition(ticks);
        events.add(Channel.SetPosition);
        events.add(Channel.SetState);
    }

    private void core_setTrackMute(final int trackIndex, final boolean muted, final Set<? super Channel> events) {
        if (backing.getTrackMute(trackIndex) != muted) {
            backing.setTrackMute(trackIndex, muted);
            core_clrModes(events);
        }

    }

    private void core_start(final Set<? super Channel> messages) {
        if (!backing.isOpen()) {
            core_open(messages);
        }

        if (!backing.isRunning()) {
            backing.start();
            messages.add(Channel.SetState);
        }

    }

    private void core_stop(final Set<? super Channel> events) {
        if (backing.isRunning()) {
            backing.stop();
            events.add(Channel.SetState);
        }

    }

    @Override
    protected final Audience audience() {
        return audience;
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
    }

    public final Mode getMode(final int index) {
        return ((0 <= index) && (index < core_Modes().length)) ? core_Modes()[index] : Mode.NORMAL;
    }

    public final long getPosition() {
        return backing.getTickPosition();
    }

    public final void setPosition(final long ticks) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        core_setTickPosition(ticks, channels);
        fire(channels);
    }

    public final MidiSequence getSequence() {
        return sequence;
    }

    public final State getState() {
        if (backing.isRunning()) {
            return State.RUN;
        } else if (backing.isOpen()) {
            return 0L == backing.getTickPosition() ? State.STOP : State.PAUSE;
        } else {
            return State.IDLE;
        }
    }

    public final void setState(final State newState) {
        setNonNull((null == newState) ? State.IDLE : newState);
    }

    private void setNonNull(final State newState) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final State currState = getState();

        if (currState != newState) {
            if (State.IDLE == currState) {
                core_open(channels);
            }

            if (State.RUN == newState) {
                core_start(channels);
            } else if (State.IDLE == newState) {
                core_close(channels);
            } else {
                core_stop(channels);
                if (State.STOP == newState) {
                    core_setTickPosition(0L, channels);
                }
            }
            fire(channels);
        }
    }

    public final int getTempo() {
        return Math.round(backing.getTempoInBPM());
    }

    public final void setTempo(final int tempo) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        channels.add(Channel.SetTempo);
        backing.setTempoInBPM(tempo);
        sequence.setTempo(tempo);
        fire(channels);
    }

    public final Timing getTiming() {
        return sequence.getTiming();
    }

    public final void setMode(final int index, final Mode newMode) {
        final Mode oldMode = getMode(index);
        if (oldMode != newMode) {
            final Set<Channel> channels = EnumSet.noneOf(Channel.class);
            final int length;
            int i;
            if (Mode.SOLO != oldMode) {
                if (Mode.SOLO == newMode) {
                    length = sequence.getTracks().size();

                    for (i = 0; i < length; ++i) {
                        core_setTrackMute(i, i != index, channels);
                    }
                } else {
                    core_setTrackMute(index, Mode.MUTE == newMode, channels);
                }
            } else {
                length = sequence.getTracks().size();

                for (i = 0; i < length; ++i) {
                    core_setTrackMute(i, i == index && Mode.MUTE == newMode, channels);
                }
            }
            fire(channels);
        }
    }

    private void onSetParts(final MidiSequence midiSequence) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final boolean running = backing.isRunning();
        final boolean open = backing.isOpen();
        final long currPos = backing.getTickPosition();
        core_close(channels);
        if (open) {
            core_setTickPosition(currPos, channels);
            if (running) {
                core_start(channels);
            }
        }
        fire(channels);
    }

    private class STARTER implements Consumer<MidiPlayer> {

        private volatile State lastState = null;

        public final void accept(final MidiPlayer player) {
            synchronized (this) {
                final State state = player.getState();
                if (State.RUN == state && state != lastState) {
                    // TODO?: Timer: static? member?
                    (new Timer()).schedule(MidiPlayer.this.new Task(), 100L, 50L);
                }
                lastState = state;
            }
        }
    }

    @SuppressWarnings("SynchronizeOnThis")
    private class Task extends TimerTask {

        private volatile int lastTempo = 0;

        public final void run() {
            final Set<Channel> channels = EnumSet.noneOf(Channel.class);
            channels.add(Channel.SetPosition);
            if (!backing.isRunning()) {
                cancel();
                channels.add(Channel.SetState);
            }

            synchronized (this) {
                final int tempo = getTempo();
                if (tempo != lastTempo) {
                    lastTempo = tempo;
                    channels.add(Channel.SetTempo);
                }
            }
            fire(channels);
        }
    }

    public enum Mode {
        NORMAL,
        SOLO,
        MUTE
    }

    public enum State {
        IDLE,
        STOP,
        PAUSE,
        RUN
    }

    public enum Channel implements de.team33.patterns.notes.alpha.Channel<MidiPlayer> {
        SetModes,
        SetPosition,
        SetState,
        SetTempo
    }
}
