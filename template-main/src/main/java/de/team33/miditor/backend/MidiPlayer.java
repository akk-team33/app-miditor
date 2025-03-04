package de.team33.miditor.backend;

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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.team33.miditor.backend.Util.CNV;

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
        if (!sequencer.isOpen()) {
            return State.OFF;
        } else if (sequencer.isRunning()) {
            return State.RUNNING;
        } else if (0L == sequencer.getTickPosition()) {
            return State.READY;
        } else {
            return State.PAUSED;
        }
    }

    private void operate(final Action action) {
        final Set<Channel<?>> results = action.apply(sequencer, state());
        fire(results);
    }

    public final void on() {
        operate(Action.ON);
    }

    public final void start() {
        operate(Action.START);
    }

    public final void stop() {
        operate(Action.STOP);
    }

    public final void pause() {
        operate(Action.PAUSE);
    }

    public final void off() {
        operate(Action.OFF);
    }

    private enum Action {

        ON(Case.by(State.OFF, Activity.OPEN)),
        START(Case.by(State.OFF, Activity.OPEN, Activity.START),
              Case.by(State.READY, Activity.START),
              Case.by(State.PAUSED, Activity.START)),
        STOP(Case.by(State.PAUSED, Activity.RESET),
             Case.by(State.RUNNING, Activity.STOP, Activity.RESET)),
        PAUSE(Case.by(State.RUNNING, Activity.STOP)),
        OFF(Case.by(State.READY, Activity.CLOSE, Activity.RESET),
            Case.by(State.RUNNING, Activity.CLOSE, Activity.RESET),
            Case.by(State.PAUSED, Activity.CLOSE, Activity.RESET));

        private final Map<State, List<Activity>> map;

        Action(final Case... cases) {
            this.map = Stream.of(cases).collect(HashMap::new, Action::put, Map::putAll);
        }

        @SuppressWarnings("BoundedWildcard")
        private static void put(final Map<State, List<Activity>> map, final Case cse) {
            map.put(cse.state, cse.methods);
        }

        private Set<Channel<?>> apply(final Sequencer sequencer, final State state) {
            return Optional.ofNullable(map.get(state))
                           .orElseGet(List::of)
                           .stream()
                           .map(activity -> activity.apply(sequencer))
                           .collect(Collectors.toSet());
        }
    }

    @FunctionalInterface
    private interface Activity extends Function<Sequencer, Channel<?>> {
        Activity OPEN = act(CNV.consumer(Sequencer::open), Channel.SET_STATE);
        Activity START = act(Sequencer::start, Channel.SET_STATE);
        Activity STOP = act(Sequencer::stop, Channel.SET_STATE);
        Activity RESET = act(seq -> seq.setTickPosition(0L), Channel.SET_POSITION);
        Activity CLOSE = act(Sequencer::close, Channel.SET_STATE);

        static Activity act(final Consumer<Sequencer> consumer, Channel<?> result) {
            return sequencer1 -> {
                consumer.accept(sequencer1);
                return result;
            };
        }
    }

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

    private record Case(State state, List<Activity> methods) {

        static Case by(final State state, final Activity... methods) {
            return new Case(state, Arrays.asList(methods));
        }
    }
}
