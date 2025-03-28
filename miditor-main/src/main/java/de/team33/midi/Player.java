package de.team33.midi;

import de.team33.midi.util.ClassUtil;
import de.team33.patterns.lazy.narvi.LazyFeatures;
import de.team33.patterns.notes.beta.Sender;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import static de.team33.midi.Util.sleep;

public class Player extends Sender<Player> {

    private static final Preferences PREFS = Preferences.userRoot().node(ClassUtil.getPathString(Player.class));
    private static final int INTERVAL = 50;

    private final Sequence sequence;
    private final Sequencer sequencer;
    private final Features features = new Features();
    private MidiDevice outputDevice;

    Player(final Sequencer sequencer, final Sequence sequence, final Executor executor) {
        super(Player.class, executor, Channel.VALUES);
        this.sequencer = sequencer;
        this.sequence = sequence;
        audience().add(Channel.SET_STATE, this::onSetState);
        push(PlayTrigger.ON);
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

    private void onSetState(final PlayState state) {
        if (PlayState.RUNNING == state) {
            //noinspection ObjectToString
            new Thread(new StateObserver(), this + ":stateObserver").start();
        }
    }

    @Deprecated // make private asap!
    final void close() {
        if (sequencer.isOpen()) {
            if (sequencer.isRunning()) {
                sequencer.stop();
            }
            sequencer.close();
            outputDevice.close();
        }
    }

    @Deprecated // make private asap!
    final void open() throws MidiUnavailableException, InvalidMidiDataException {
        if (!sequencer.isOpen()) {
            outputDevice = newOutputDevice();
            outputDevice.open();
            sequencer.getTransmitter().setReceiver(outputDevice.getReceiver());
            sequencer.setSequence(sequence);
            sequencer.open();
        }
    }

    public final TrackMode getMode(final int index) {
        final List<TrackMode> trackModes = features.get(Key.TRACK_MODES);
        return (0 <= index) && (index < trackModes.size()) ? trackModes.get(index) : TrackMode.NORMAL;
    }

    public final long getPosition() {
        return sequencer.getTickPosition();
    }

    public final void setPosition(final long ticks) {
        sequencer.setTickPosition(ticks);
        fire(Channel.SET_POSITION);
    }

    public final PlayState getState() {
        return PlayState.of(sequencer);
    }

    public final void push(final PlayTrigger trigger) {
        final Set<Channel<?>> results = trigger.apply(this, getState());
        fire(results);
    }

    public final int getTempo() {
        return Math.round(sequencer.getTempoInBPM());
    }

    public final void setTempo(final int tempo) {
        sequencer.setTempoInBPM(tempo);
        fire(Channel.SET_TEMPO);
    }

    public final void setMode(final int index, final TrackMode newMode) {
        final Set<Channel<?>> channels = new HashSet<>(0);
        final TrackMode oldMode = getMode(index);
        if (oldMode != newMode) {
            if (TrackMode.SOLO == newMode) {
                IntStream.range(0, Util.tracksSize(sequencer))
                         .forEach(ix -> sequencer.setTrackMute(ix, ix != index));
            } else if ((TrackMode.SOLO == oldMode) && (TrackMode.NORMAL == newMode)) {
                IntStream.range(0, Util.tracksSize(sequencer))
                         .forEach(ix -> sequencer.setTrackMute(ix, false));
            } else {
                sequencer.setTrackMute(index, TrackMode.MUTE == newMode);
            }
            features.reset(Key.TRACK_MODES);
            channels.add(Channel.SET_MODES);
        }
        fire(channels);
    }

    final void onSetParts() {
        final boolean open = sequencer.isOpen();
        final boolean running = sequencer.isRunning();
        final long position = sequencer.getTickPosition();
        close();
        if (open) {
            Util.CNV.run(this::open);
            sequencer.setTickPosition(position);
            if (running) {
                sequencer.start();
            }
        }
        fire(Channel.SET_STATE, Channel.SET_POSITION);
    }

    final Sequencer backing() {
        return sequencer;
    }

    @FunctionalInterface
    @SuppressWarnings("ClassNameSameAsAncestorName")
    public interface Channel<M> extends Sender.Channel<Player, M> {

        Channel<Player> SET_MODES = midiPlayer -> midiPlayer;
        Channel<Player> SET_POSITION = midiPlayer -> midiPlayer;
        Channel<PlayState> SET_STATE = Player::getState;
        Channel<Player> SET_TEMPO = midiPlayer -> midiPlayer;

        @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
        Set<Channel<?>> VALUES = Set.of(SET_MODES, SET_POSITION, SET_STATE, SET_TEMPO);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    interface Key<R> extends LazyFeatures.Key<Features, R> {

        Key<List<TrackMode>> TRACK_MODES = Features::newTrackModes;
    }

    private final class StateObserver implements Runnable {

        private long lastPosition = sequencer.getTickPosition();
        private float lastTempo = sequencer.getTempoInBPM();

        @Override
        public void run() {
            do {
                sleep(INTERVAL);
                checkTempo();
                checkPosition();
            } while (sequencer.isRunning());
            checkState();
        }

        private void checkState() {
            if (sequencer.getTickPosition() >= sequencer.getTickLength()) {
                fire(Channel.SET_STATE);
            }
        }

        private void checkPosition() {
            final long newPosition = sequencer.getTickPosition();
            if (newPosition != lastPosition) {
                lastPosition = newPosition;
                fire(Channel.SET_POSITION);
            }
        }

        private void checkTempo() {
            final float newTempo = sequencer.getTempoInBPM();
            if (newTempo != lastTempo) {
                lastTempo = newTempo;
                fire(Channel.SET_TEMPO);
            }
        }
    }

    private final class Features extends LazyFeatures<Features> {

        @Override
        protected final Features host() {
            return this;
        }

        private List<TrackMode> newTrackModes() {
            final List<TrackMode> stage = IntStream.range(0, Util.tracksSize(sequencer))
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
            return sequencer.getTrackMute(index) ? TrackMode.MUTE : TrackMode.NORMAL;
        }
    }
}
