package de.team33.miditor.backend;

import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.Sequencer;
import java.util.Arrays;
import java.util.HashMap;
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

import static de.team33.miditor.backend.Util.CNV;
import static java.util.function.Predicate.not;

@SuppressWarnings("UnusedReturnValue")
public class MidiPlayer extends Sender<MidiPlayer> {

    private final Sequencer sequencer;
    private final Mapping mapping;

    MidiPlayer(final Context ctx) {
        super(ctx.audience(), MidiPlayer.class);
        this.sequencer = ctx.sequencer();
        this.mapping = Mapping.builder()
                              .put(Channel.SET_STATE, this::state)
                              .put(Channel.SET_POSITION, sequencer::getTickPosition)
                              .build();
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
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

    @SuppressWarnings("InterfaceWithOnlyOneDirectInheritor")
    interface Context {

        Audience audience();

        Sequencer sequencer();
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
    }
}
