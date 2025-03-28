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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class Player extends Sender<Player> {

    private static final Preferences PREFS = Preferences.userRoot().node(ClassUtil.getPathString(Player.class));

    private final Sequence sequence;
    private final Sequencer sequencer;
    private MidiDevice outputDevice;
    private final Features features = new Features();

    Player(final Sequencer sequencer, final Sequence sequence, final Executor executor) {
        super(Player.class, executor, Channel.VALUES);
        this.sequencer = sequencer;
        this.sequence = sequence;
        audience().add(Channel.SET_STATE, new STARTER());
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
        final Set<Channel> results = trigger.apply(this, getState());
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
        final Set<Channel> channels = new HashSet<>(0);
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

    @SuppressWarnings("SynchronizeOnThis")
    private class STARTER implements Consumer<Player> {

        private volatile PlayState lastState = null;

        public final void accept(final Player player) {
            synchronized (this) {
                final PlayState state = player.getState();
                if (PlayState.RUNNING == state && state != lastState) {
                    // TODO?: Timer: static? member?
                    (new Timer()).schedule(Player.this.new Task(), 100L, 50L);
                }
                lastState = state;
            }
        }
    }

    @SuppressWarnings("SynchronizeOnThis")
    private class Task extends TimerTask {

        private volatile int lastTempo = 0;

        public final void run() {
            final Set<Channel> channels = new HashSet<>(0);
            channels.add(Channel.SET_POSITION);
            if (!sequencer.isRunning()) {
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

    @FunctionalInterface
    @SuppressWarnings("ClassNameSameAsAncestorName")
    public interface Channel extends Sender.Channel<Player, Player> {

        Channel SET_MODES = midiPlayer -> midiPlayer;
        Channel SET_POSITION = midiPlayer -> midiPlayer;
        Channel SET_STATE = midiPlayer -> midiPlayer;
        Channel SET_TEMPO = midiPlayer -> midiPlayer;

        @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
        Set<Channel> VALUES = Set.of(SET_MODES, SET_POSITION, SET_STATE, SET_TEMPO);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    interface Key<R> extends LazyFeatures.Key<Features, R> {

        Key<List<TrackMode>> TRACK_MODES = Features::newTrackModes;
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
