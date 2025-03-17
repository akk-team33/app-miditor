package de.team33.midi;

import de.team33.patterns.enums.pan.Values;

import javax.sound.midi.Sequencer;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

public enum PlayState {

    OFF(not(Sequencer::isOpen)),
    RUNNING(Sequencer::isRunning),
    PAUSED(sequencer -> 0L < sequencer.getTickPosition()),
    READY(sequencer -> true);

    private static final Values<PlayState> VALUES = Values.of(PlayState.class);

    private final Predicate<Sequencer> condition;

    PlayState(final Predicate<Sequencer> condition) {
        this.condition = condition;
    }

    @Deprecated // make package private
    public static PlayState of(final Sequencer sequencer) {
        return VALUES.findAny(state -> state.condition.test(sequencer))
                     .orElse(READY);
    }
}
