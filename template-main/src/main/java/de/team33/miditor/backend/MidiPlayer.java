package de.team33.miditor.backend;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

public abstract class MidiPlayer {

    abstract Sequencer sequencer();

    public final State state() {
        if (!sequencer().isOpen()) {
            return State.IDLE;
        } else if (sequencer().isRunning()) {
            return State.RUN;
        } else if (0L == sequencer().getTickPosition()) {
            return State.STOP;
        } else {
            return State.PAUSE;
        }
    }

    public final void on() {
        if (!sequencer().isOpen()) {
            try {
                sequencer().open();
                // TODO: rise event!
            } catch (final MidiUnavailableException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public final void start() {
        if (State.RUN != state()) {
            on();
            sequencer().start();
            // TODO: rise event!
        }
    }

    public final void stop() {
        if (State.RUN == state()) {
            sequencer().stop();
            sequencer().setTickPosition(0L);
            // TODO: rise event!
        }
    }

    public final void pause() {
        if (State.RUN == state()) {
            sequencer().stop();
            // TODO: rise event!
        }
    }

    public final void off() {
        if (sequencer().isOpen()) {
            sequencer().close();
            // TODO: rise event!
        }
    }

    public enum State {
        IDLE,
        STOP,
        PAUSE,
        RUN;
    }
}
