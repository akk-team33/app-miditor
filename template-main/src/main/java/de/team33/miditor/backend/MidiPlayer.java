package de.team33.miditor.backend;

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
import java.util.stream.Stream;

import static de.team33.miditor.backend.Midi.MetaMessage.Type.SET_TEMPO;
import static de.team33.miditor.backend.Util.CNV;
import static de.team33.miditor.backend.Util.sleep;
import static java.util.function.Predicate.not;

@SuppressWarnings("UnusedReturnValue")
public class MidiPlayer extends Sender<MidiPlayer> {

    private static final int INTERVAL = 50;

    private final Audience audience;
    private final Sequencer sequencer;
    private final Mapping mapping;

    MidiPlayer(final Audience audience, final Sequencer sequencer) {
        super(MidiPlayer.class);
        this.audience = audience;
        this.sequencer = sequencer;
        this.mapping = Mapping.builder()
                              .put(Channel.SET_STATE, this::state)
                              .put(Channel.SET_POSITION, this::position)
                              .put(Channel.SET_TEMPO, this::tempo)
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
        if (SET_TEMPO.value() == metaMessage.getType()) {
            fire(Channel.SET_TEMPO);
        }
    }

    @Override
    protected Audience audience() {
        return audience;
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
    }

    public final int tempo() {
        return Math.round(sequencer.getTempoInBPM());
    }

    public final MidiPlayer setTempo(final int tempo) {
        sequencer.setTempoInBPM(tempo);
        // TODO?: this.sequence.setTempo(tempo);
        return fire(Channel.SET_TEMPO);
    }

    public final long position() {
        return sequencer.getTickPosition();
    }

    public final MidiPlayer setPosition(final long newPosition) {
        final Set<Channel<?>> channels = new HashSet<>(0);
        final long oldPosition = position();
        if (newPosition != oldPosition) {
            final State oldState = state();
            sequencer.setTickPosition(newPosition);
            channels.add(Channel.SET_POSITION);
            if (oldState != state()) {
                channels.add(Channel.SET_STATE);
            }
        }
        return fire(channels);
    }

    public final State state() {
        return State.of(sequencer);
    }

    public final MidiPlayer act(final Trigger trigger) {
        final Set<Channel<?>> results = trigger.apply(sequencer, state());
        return fire(results);
    }

    @SuppressWarnings("WeakerAccess")
    public enum Trigger {

        ON(Choice.by(State.OFF, Action.OPEN)),
        START(Choice.by(State.OFF, Action.OPEN, Action.START),
              Choice.by(State.READY, Action.START),
              Choice.by(State.PAUSED, Action.START)),
        STOP(Choice.by(State.PAUSED, Action.RESET),
             Choice.by(State.RUNNING, Action.STOP, Action.RESET)),
        PAUSE(Choice.by(State.RUNNING, Action.STOP)),
        OFF(Choice.by(State.READY, Action.CLOSE, Action.RESET),
            Choice.by(State.RUNNING, Action.CLOSE, Action.RESET),
            Choice.by(State.PAUSED, Action.CLOSE, Action.RESET));

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

            static Choice by(final State state, final Action... methods) {
                return new Choice(state, Arrays.asList(methods));
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
    }
}
