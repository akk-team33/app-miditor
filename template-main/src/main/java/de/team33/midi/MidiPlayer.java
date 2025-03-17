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
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

@SuppressWarnings("ClassWithTooManyMethods")
public class MidiPlayer extends Sender<MidiPlayer> {

    private static final Preferences PREFS = Preferences.userRoot().node(ClassUtil.getPathString(MidiPlayer.class));

    private final Audience audience;
    private final Mapping mapping;
    private final MidiSequence sequence;
    private final Sequencer backing;
    private MidiDevice outputDevice;
    private final Features features = new Features();

    public MidiPlayer(final MidiSequence sequence) throws MidiUnavailableException {
        super(MidiPlayer.class);
        this.audience = new Audience(new SimpleAsyncExecutor());
        this.mapping = Mapping.builder()
                              .put(Channel.SET_POSITION, () -> this)
                              .put(Channel.SET_STATE, () -> this)
                              .put(Channel.SET_TEMPO, () -> this)
                              .put(Channel.SET_MODES, () -> this)
                              .build();
        backing = MidiSystem.getSequencer(false);
        this.sequence = sequence;
        this.sequence.add(MidiSequence.Channel.SetTracks, this::onSetParts);
        audience.add(Channel.SET_STATE, new STARTER());
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
            events.add(Channel.SET_STATE);
        }
    }

    private void core_open(final Set<? super Channel> events) {
        if (!backing.isOpen()) {
            try {
                outputDevice = newOutputDevice();
                outputDevice.open();
                backing.getTransmitter().setReceiver(outputDevice.getReceiver());
                backing.setSequence(sequence.backing());
                backing.open();
                events.add(Channel.SET_STATE);
                events.add(Channel.SET_TEMPO);
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
        events.add(Channel.SET_POSITION);
        events.add(Channel.SET_STATE);
    }

    private void core_start(final Set<? super Channel> messages) {
        if (!backing.isOpen()) {
            core_open(messages);
        }

        if (!backing.isRunning()) {
            backing.start();
            messages.add(Channel.SET_STATE);
        }

    }

    private void core_stop(final Set<? super Channel> events) {
        if (backing.isRunning()) {
            backing.stop();
            events.add(Channel.SET_STATE);
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

    public final TrackMode getMode(final int index) {
        final List<TrackMode> trackModes = features.get(Key.TRACK_MODES);
        return (0 <= index) && (index < trackModes.size()) ? trackModes.get(index) : TrackMode.NORMAL;
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

    public final PlayState getState() {
        return PlayState.of(backing);
    }

    public final void setState(final PlayState newState) {
        setNonNull((null == newState) ? PlayState.OFF : newState);
    }

    private void setNonNull(final PlayState newState) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final PlayState currState = getState();

        if (currState != newState) {
            if (PlayState.OFF == currState) {
                core_open(channels);
            }

            if (PlayState.RUNNING == newState) {
                core_start(channels);
            } else if (PlayState.OFF == newState) {
                core_close(channels);
            } else {
                core_stop(channels);
                if (PlayState.READY == newState) {
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
        channels.add(Channel.SET_TEMPO);
        backing.setTempoInBPM(tempo);
        sequence.setTempo(tempo);
        fire(channels);
    }

    public final Timing getTiming() {
        return sequence.getTiming();
    }

    public final void setMode(final int index, final TrackMode newMode) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final TrackMode oldMode = getMode(index);
        if (oldMode != newMode) {
            if (TrackMode.SOLO == newMode) {
                IntStream.range(0, Util.tracksSize(backing))
                         .forEach(ix -> backing.setTrackMute(ix, ix != index));
            } else if ((TrackMode.SOLO == oldMode) && (TrackMode.NORMAL == newMode)) {
                IntStream.range(0, Util.tracksSize(backing))
                         .forEach(ix -> backing.setTrackMute(ix, false));
            } else {
                backing.setTrackMute(index, TrackMode.MUTE == newMode);
            }
            features.reset(Key.TRACK_MODES);
            channels.add(Channel.SET_MODES);
        }
        fire(channels);
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

        private volatile PlayState lastState = null;

        public final void accept(final MidiPlayer player) {
            synchronized (this) {
                final PlayState state = player.getState();
                if (PlayState.RUNNING == state && state != lastState) {
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
            channels.add(Channel.SET_POSITION);
            if (!backing.isRunning()) {
                cancel();
                channels.add(Channel.SET_STATE);
            }

            synchronized (this) {
                final int tempo = getTempo();
                if (tempo != lastTempo) {
                    lastTempo = tempo;
                    channels.add(Channel.SET_TEMPO);
                }
            }
            fire(channels);
        }
    }

    public enum Channel implements de.team33.patterns.notes.alpha.Channel<MidiPlayer> {
        SET_MODES,
        SET_POSITION,
        SET_STATE,
        SET_TEMPO
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<Features, R> {

        Key<List<TrackMode>> TRACK_MODES = Features::newTrackModes;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends de.team33.patterns.features.alpha.Features<Features> {

        private Features() {
            super(ConcurrentHashMap::new);
        }

        @Override
        protected final Features host() {
            return this;
        }

        private List<TrackMode> newTrackModes() {
            final List<TrackMode> stage = IntStream.range(0, Util.tracksSize(backing))
                                                   .mapToObj(this::mapMode)
                                                   .toList();
            final boolean normal = (1L != stage.stream()
                                               .filter(TrackMode.NORMAL::equals)
                                               .count());
            return normal ? stage : stage.stream()
                                         .map(mode -> (TrackMode.NORMAL == mode) ? TrackMode.SOLO : mode)
                                         .toList();
        }

        private TrackMode mapMode(final int index) {
            return backing.getTrackMute(index) ? TrackMode.MUTE : TrackMode.NORMAL;
        }
    }
}
