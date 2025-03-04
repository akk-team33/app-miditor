package de.team33.miditor.backend;

import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.exceptional.dione.ExpectationException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Sequencer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.team33.miditor.backend.Util.*;
import static java.util.function.Predicate.*;

public enum State {

    OFF(not(MidiDevice::isOpen), Switching.OFF),
    RUNNING(Sequencer::isRunning, Switching.RUNNING),
    READY(sequencer -> 0L == sequencer.getTickPosition(), Switching.READY),
    PAUSED(sequencer -> true, Switching.PAUSED);

    private static final Values<State> VALUES = Values.of(State.class);

    private final Predicate<Sequencer> condition;
    private final Switching switching;

    State(final Predicate<Sequencer> condition, final Switching switching) {
        this.condition = condition;
        this.switching = switching;
    }

    static State of(final Sequencer sequencer) {
        return VALUES.findAny(state -> state.condition.test(sequencer))
                     .orElseThrow(() -> new ExpectationException("should not happen at all"));
    }

    final Function<Sequencer, Boolean> switchTo(final State newState) {
        return switching.apply(newState);
    }

    private interface Switching extends Function<State, Function<Sequencer, Boolean>> {

        Function<Sequencer, Boolean> NOTHING = run();

        Switching OFF = newState -> switch (newState) {
            case OFF -> NOTHING;
            case RUNNING -> run(Switching::on, Sequencer::start);
            default -> run(Switching::on);
        };

        Switching READY = newState -> switch (newState) {
            case OFF -> run(Sequencer::close);
            case RUNNING -> run(Sequencer::start);
            default -> NOTHING;
        };

        Switching RUNNING = newState -> switch (newState) {
            case OFF -> run(Sequencer::close);
            case PAUSED -> run(Sequencer::stop);
            case READY -> run(Sequencer::stop, Switching::reset);
            default -> NOTHING;
        };

        Switching PAUSED = newState -> switch (newState) {
            case OFF -> run(Sequencer::close);
            case RUNNING -> run(Sequencer::start);
            case READY -> run(Switching::reset);
            default -> NOTHING;
        };

        @SafeVarargs
        static Function<Sequencer, Boolean> run(final Consumer<Sequencer>... methods) {
            return sequencer -> {
                for (final Consumer<Sequencer> method : methods) {
                    method.accept(sequencer);
                }
                return 0 < methods.length;
            };
        }

        static void on(final Sequencer sequencer) {
            CNV.run(sequencer::open);
        }

        static void reset(final Sequencer sequencer) {
            sequencer.setTickPosition(0L);
        }
    }
}
