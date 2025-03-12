package de.team33.midi.impl;

import de.team33.midi.Player;
import de.team33.midi.Sequence;
import de.team33.midi.util.ClassUtil;
import de.team33.midix.Timing;
import de.team33.patterns.notes.alpha.Audience;

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
public class PlayerImpl implements Player {
    private static final Preferences PREFS = Preferences.userRoot().node(ClassUtil.getPathString(PlayerImpl.class));
    private static final Mode[] EMPTY_MODES = new Mode[0];

    private final Sequence sequence;
    private final Sequencer sequencer;
    private final Audience audience = new Audience();
    private MidiDevice outputDevice;
    private Player.Mode[] modes;

    public PlayerImpl(final Sequence sequence) throws MidiUnavailableException {
        sequencer = MidiSystem.getSequencer(false);
        this.sequence = sequence;
        this.sequence.addListener(Sequence.Channel.SetParts, this::onSetParts);
        audience.add(Event.SetState, new STARTER());
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

    private void core_close(final Set<? super Event> events) {
        if (sequencer.isOpen()) {
            outputDevice.close();
            outputDevice = null;
            sequencer.close();
            events.add(Event.SetState);
        }
    }

    private void core_clrModes(final Set<? super Event> events) {
        modes = null;
        events.add(Event.SetModes);
    }

    private Player.Mode[] core_Modes() {
        if (null == modes) {
            final javax.sound.midi.Sequence seq = sequencer.getSequence();
            if (null != seq) {
                final int length = seq.getTracks().length;
                int nNormal = 0;
                int iNormal = -1;
                modes = new Player.Mode[length];

                for (int i = 0; i < length; ++i) {
                    if (sequencer.getTrackMute(i)) {
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

    private void core_open(final Set<? super Event> events) {
        if (!sequencer.isOpen()) {
            try {
                outputDevice = newOutputDevice();
                outputDevice.open();
                sequencer.getTransmitter().setReceiver(outputDevice.getReceiver());
                sequence.associate(sequencer);
                sequencer.open();
                events.add(Event.SetState);
                events.add(Event.SetTempo);
            } catch (final Exception e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }

    }

    private void core_setTickPosition(final long ticks, final Set<? super Event> events) {
        if (!sequencer.isOpen()) {
            core_open(events);
        }

        sequencer.setTickPosition(ticks);
        events.add(Event.SetPosition);
        events.add(Event.SetState);
    }

    private void core_setTrackMute(final int trackIndex, final boolean muted, final Set<? super Event> events) {
        if (sequencer.getTrackMute(trackIndex) != muted) {
            sequencer.setTrackMute(trackIndex, muted);
            core_clrModes(events);
        }

    }

    private void core_start(final Set<? super Event> messages) {
        if (!sequencer.isOpen()) {
            core_open(messages);
        }

        if (!sequencer.isRunning()) {
            sequencer.start();
            messages.add(Event.SetState);
        }

    }

    private void core_stop(final Set<? super Event> events) {
        if (sequencer.isRunning()) {
            sequencer.stop();
            events.add(Event.SetState);
        }

    }

    public final Player.Mode getMode(final int index) {
        return ((0 <= index) && (index < core_Modes().length)) ? core_Modes()[index] : Mode.NORMAL;
    }

    public final long getPosition() {
        return sequencer.getTickPosition();
    }

    public final void setPosition(final long ticks) {
        final Set<Event> events = EnumSet.noneOf(Event.class);
        core_setTickPosition(ticks, events);
        events.forEach(message -> audience.send(message, this));
    }

    @Override
    public final void addListener(final Event event, final Consumer<? super Player> listener) {
        audience.add(event, listener);
        listener.accept(this);
    }

    public final Sequence getSequence() {
        return sequence;
    }

    public final Player.State getState() {
        if (sequencer.isRunning()) {
            return State.RUN;
        } else if (sequencer.isOpen()) {
            return 0L == sequencer.getTickPosition() ? State.STOP : State.PAUSE;
        } else {
            return State.IDLE;
        }
    }

    public final void setState(final Player.State newState) {
        setNonNull((null == newState) ? State.IDLE : newState);
    }

    private void setNonNull(final Player.State newState) {
        final Set<Event> events = EnumSet.noneOf(Event.class);
        final Player.State currState = getState();

        if (currState != newState) {
            if (State.IDLE == currState) {
                core_open(events);
            }

            if (State.RUN == newState) {
                core_start(events);
            } else if (State.IDLE == newState) {
                core_close(events);
            } else {
                core_stop(events);
                if (State.STOP == newState) {
                    core_setTickPosition(0L, events);
                }
            }
            events.forEach(event -> audience.send(event, this));
        }
    }

    public final int getTempo() {
        return Math.round(sequencer.getTempoInBPM());
    }

    public final void setTempo(final int tempo) {
        final Set<Event> events = EnumSet.noneOf(Event.class);
        events.add(Event.SetTempo);
        sequencer.setTempoInBPM(tempo);
        sequence.setTempo(tempo);
        events.forEach(event -> audience.send(event, this));
    }

    public final Timing getTiming() {
        return sequence.getTiming();
    }

    public final void setMode(final int index, final Player.Mode newMode) {
        final Player.Mode oldMode = getMode(index);
        if (oldMode != newMode) {
            final Set<Event> events = EnumSet.noneOf(Event.class);
            final int length;
            int i;
            if (Mode.SOLO != oldMode) {
                if (Mode.SOLO == newMode) {
                    length = sequence.getTracks().length;

                    for (i = 0; i < length; ++i) {
                        core_setTrackMute(i, i != index, events);
                    }
                } else {
                    core_setTrackMute(index, Mode.MUTE == newMode, events);
                }
            } else {
                length = sequence.getTracks().length;

                for (i = 0; i < length; ++i) {
                    core_setTrackMute(i, i == index && Mode.MUTE == newMode, events);
                }
            }
            events.forEach(event -> audience.send(event, this));
        }
    }

    private void onSetParts(final Sequence sequence) {
        final Set<Event> events = EnumSet.noneOf(Event.class);
        final boolean running = sequencer.isRunning();
        final boolean open = sequencer.isOpen();
        final long currPos = sequencer.getTickPosition();
        core_close(events);
        if (open) {
            core_setTickPosition(currPos, events);
            if (running) {
                core_start(events);
            }
        }
        events.forEach(event -> audience.send(event, this));
    }

    private class STARTER implements Consumer<Player> {

        private volatile Player.State lastState = null;

        public final void accept(final Player player) {
            synchronized (this) {
                final State state = player.getState();
                if (State.RUN == state && state != lastState) {
                    // TODO?: Timer: static? member?
                    (new Timer()).schedule(PlayerImpl.this.new Task(), 100L, 50L);
                }
                lastState = state;
            }
        }
    }

    @SuppressWarnings("SynchronizeOnThis")
    private class Task extends TimerTask {

        private volatile int lastTempo = 0;

        public final void run() {
            final Set<Event> events = EnumSet.noneOf(Event.class);
            events.add(Event.SetPosition);
            if (!sequencer.isRunning()) {
                cancel();
                events.add(Event.SetState);
            }

            synchronized (this) {
                final int tempo = getTempo();
                if (tempo != lastTempo) {
                    lastTempo = tempo;
                    events.add(Event.SetTempo);
                }
            }
            events.forEach(event -> audience.send(event, PlayerImpl.this));
        }
    }
}
