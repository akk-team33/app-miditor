package de.team33.midix;

import de.team33.midi.TrackMode;
import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequencer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.team33.midix.Midi.MetaMessage.Type.SET_TEMPO;
import static de.team33.midix.Util.CNV;
import static de.team33.midix.Util.sleep;
import static java.util.function.Predicate.not;

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

    private void onSetState(final State state) {
        if (State.RUNNING == state) {
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
            final State oldState = getState();
            sequencer.setTickPosition(newPosition);
            channels.add(Channel.SET_POSITION);
            if (oldState != getState()) {
                channels.add(Channel.SET_STATE);
            }
        }
        return fire(channels);
    }

    public final State getState() {
        return State.of(sequencer);
    }

    public final MidiPlayer push(final Trigger trigger) {
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

    @SuppressWarnings("WeakerAccess")
    public enum Trigger {

        ON(Choice.on(State.OFF).apply(Action.OPEN)),
        START(Choice.on(State.OFF).apply(Action.OPEN, Action.START),
              Choice.on(State.READY).apply(Action.START),
              Choice.on(State.PAUSED).apply(Action.START)),
        STOP(Choice.on(State.PAUSED).apply(Action.RESET),
             Choice.on(State.RUNNING).apply(Action.STOP, Action.RESET)),
        PAUSE(Choice.on(State.RUNNING).apply(Action.STOP)),
        OFF(Choice.on(State.READY).apply(Action.CLOSE, Action.RESET),
            Choice.on(State.RUNNING).apply(Action.CLOSE, Action.RESET),
            Choice.on(State.PAUSED).apply(Action.CLOSE, Action.RESET));

        private static final Values<Trigger> VALUES = Values.of(Trigger.class);
        private static final Map<State, Set<Trigger>> effectiveMap = new ConcurrentHashMap<>(0);

        private final Map<State, List<Action>> map;

        Trigger(final Choice... choices) {
            this.map = Stream.of(choices).collect(HashMap::new, Trigger::put, Map::putAll);
        }

        private static void put(final Map<? super State, ? super List<Action>> map, final Choice choice) {
            map.put(choice.state, choice.methods);
        }

        public static Set<Trigger> allEffectiveOn(final State state) {
            return effectiveMap.computeIfAbsent(state, Trigger::newEffectiveSet);
        }

        private static Set<Trigger> newEffectiveSet(final State state) {
            return VALUES.stream()
                         .filter(value -> value.hasEffectOn(state))
                         .collect(Collectors.toUnmodifiableSet());
        }

        private Set<Channel<?>> apply(final Sequencer sequencer, final State state) {
            return Optional.ofNullable(map.get(state))
                           .orElseGet(List::of)
                           .stream()
                           .map(action -> action.apply(sequencer))
                           .collect(Collectors.toSet());
        }

        public final boolean hasEffectOn(final State state) {
            return map.containsKey(state);
        }

        @FunctionalInterface
        private interface Action extends Function<Sequencer, Channel<?>> {

            Action OPEN = act(CNV.consumer(Sequencer::open), Channel.SET_STATE);
            Action START = act(Sequencer::start, Channel.SET_STATE);
            Action STOP = act(Sequencer::stop, Channel.SET_STATE);
            Action RESET = act(seq -> seq.setTickPosition(0L), Channel.SET_POSITION);
            Action CLOSE = act(Sequencer::close, Channel.SET_STATE);

            @SuppressWarnings("BoundedWildcard")
            static Action act(final Consumer<Sequencer> consumer, final Channel<?> result) {
                return sequencer -> {
                    consumer.accept(sequencer);
                    return result;
                };
            }
        }

        private record Choice(State state, List<Action> methods) {

            static Stage on(final State state) {
                return actions -> new Choice(state, Arrays.asList(actions));
            }

            private interface Stage {
                Choice apply(Action... actions);
            }
        }
    }

    public enum State {

        OFF(not(Sequencer::isOpen)),
        RUNNING(Sequencer::isRunning),
        PAUSED(sequencer -> 0L < sequencer.getTickPosition()),
        READY(sequencer -> true);

        private static final Values<State> VALUES = Values.of(State.class);

        private final Predicate<Sequencer> condition;

        State(final Predicate<Sequencer> condition) {
            this.condition = condition;
        }

        private static State of(final Sequencer sequencer) {
            return VALUES.findAny(state -> state.condition.test(sequencer))
                         .orElse(READY);
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    public interface Channel<M> extends de.team33.patterns.notes.alpha.Channel<M> {

        /**
         * Symbolizes a change of the current player state.
         */
        Channel<State> SET_STATE = () -> "SET_STATE";

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
