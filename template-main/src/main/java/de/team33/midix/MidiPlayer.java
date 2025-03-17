package de.team33.midix;

import de.team33.midi.PlayState;
import de.team33.midi.TrackMode;
import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequencer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static de.team33.midix.Midi.MetaMessage.Type.SET_TEMPO;
import static de.team33.midix.Util.sleep;

@SuppressWarnings("UnusedReturnValue")
public class MidiPlayer extends Sender<MidiPlayer> {

    private static final int INTERVAL = 50;

    private final Audience audience;
    private final Sequencer sequencer;
    private final Mapping mapping;
    private final Features features = new Features();

    MidiPlayer(final Audience audience, final Sequencer sequencer) {
        super(MidiPlayer.class);
        this.audience = audience;
        this.sequencer = sequencer;
        this.mapping = Mapping.builder()
                              .put(Channel.SET_STATE, this::getState)
                              .put(Channel.SET_POSITION, this::getPosition)
                              .put(Channel.SET_TEMPO, this::getTempo)
                              .put(Channel.SET_TRACK_MODE, this::getTrackModes)
                              .build();
        sequencer.addMetaEventListener(this::onMetaEvent);
        audience.add(Channel.SET_STATE, this::onSetState);
    }

    private void onSetState(final PlayState state) {
        if (PlayState.RUNNING == state) {
            new Thread(this::whileRunning, "TODO:name").start();
        }
    }

    private void whileRunning() {
        sleep(INTERVAL);
        while (sequencer.isRunning()) {
            fire(Channel.SET_POSITION);
            sleep(INTERVAL);
        }
        fire(Channel.SET_POSITION, Channel.SET_STATE);
    }

    private void onMetaEvent(final MetaMessage metaMessage) {
        if (SET_TEMPO.isValid(metaMessage)) {
            fire(Channel.SET_TEMPO);
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

    public final int getTempo() {
        return Math.round(sequencer.getTempoInBPM());
    }

    public final MidiPlayer setTempo(final int tempo) {
        sequencer.setTempoInBPM(tempo);
        // TODO?: this.sequence.setTempo(tempo);
        return fire(Channel.SET_TEMPO);
    }

    public final long getPosition() {
        return sequencer.getTickPosition();
    }

    public final MidiPlayer setPosition(final long newPosition) {
        final Set<Channel<?>> channels = new HashSet<>(0);
        final long oldPosition = getPosition();
        if (newPosition != oldPosition) {
            final PlayState oldState = getState();
            sequencer.setTickPosition(newPosition);
            channels.add(Channel.SET_POSITION);
            if (oldState != getState()) {
                channels.add(Channel.SET_STATE);
            }
        }
        return fire(channels);
    }

    public final PlayState getState() {
        return PlayState.of(sequencer);
    }

    public final MidiPlayer push(final PlayTrigger trigger) {
        final Set<Channel<?>> results = trigger.apply(sequencer, getState());
        return fire(results);
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

    public final List<TrackMode> getTrackModes() {
        return features.get(Key.TRACK_MODES);
    }

    public final TrackMode getTrackMode(final int index) {
        return getTrackModes().get(index);
    }

    public final MidiPlayer setTrackMode(final int index, final TrackMode newMode) {
        final Set<Channel<?>> channels = new HashSet<>(1);
        final TrackMode oldMode = features.get(Key.TRACK_MODES).get(index);
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
            channels.add(Channel.SET_TRACK_MODE);
        }
        return fire(channels);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    public interface Channel<M> extends de.team33.patterns.notes.alpha.Channel<M> {

        /**
         * Symbolizes a change of the current player state.
         */
        Channel<PlayState> SET_STATE = () -> "SET_STATE";

        /**
         * Symbolizes a change of the current player position.
         */
        Channel<Long> SET_POSITION = () -> "SET_POSITION";

        /**
         * Symbolizes a change of the current player tempo.
         */
        Channel<Integer> SET_TEMPO = () -> "SET_TEMPO";

        /**
         * Symbolizes a change of the current player track modes.
         */
        Channel<List<TrackMode>> SET_TRACK_MODE = () -> "SET_TRACK_MODE";
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<MidiPlayer, R> {

        Key<List<TrackMode>> TRACK_MODES = MidiPlayer::newTrackModes;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends de.team33.patterns.features.alpha.Features<MidiPlayer> {

        private Features() {
            super(ConcurrentHashMap::new);
        }

        @Override
        protected final MidiPlayer host() {
            return MidiPlayer.this;
        }
    }
}
