package de.team33.miditor.backend;

import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.Sequencer;

import java.util.function.Consumer;

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
            return State.IDLE;
        } else if (sequencer.isRunning()) {
            return State.RUN;
        } else if (0L == sequencer.getTickPosition()) {
            return State.STOP;
        } else {
            return State.PAUSE;
        }
    }

    public final void on() {
        if (!sequencer.isOpen()) {
            CNV.run(sequencer::open);
            fire(Channel.SET_STATE);
        }
    }

    public final void start() {
        if (!sequencer.isRunning()) {
            on();
            sequencer.start();
            fire(Channel.SET_STATE);
        }
    }

    public final void stop() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            sequencer.setTickPosition(0L);
            fire(Channel.SET_STATE, Channel.SET_POSITION);
        }
    }

    public final void pause() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            fire(Channel.SET_STATE);
        }
    }

    public final void off() {
        if (sequencer.isOpen()) {
            sequencer.close();
            fire(Channel.SET_STATE);
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
}
