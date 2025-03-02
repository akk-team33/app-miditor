package de.team33.miditor.backend;

import static de.team33.miditor.backend.MidiCenter.CNV;

public abstract class MidiPlayer {

    abstract MidiCenter center();

    public final State state() {
        if (!center().sequencer.isOpen()) {
            return State.IDLE;
        } else if (center().sequencer.isRunning()) {
            return State.RUN;
        } else if (0L == center().sequencer.getTickPosition()) {
            return State.STOP;
        } else {
            return State.PAUSE;
        }
    }

    public final void on() {
        if (!center().sequencer.isOpen()) {
            CNV.run(() -> center().sequencer.open());
            // TODO: rise event!
        }
    }

    public final void start() {
        if (State.RUN != state()) {
            on();
            center().sequencer.start();
            // TODO: rise event!
        }
    }

    public final void stop() {
        if (State.RUN == state()) {
            center().sequencer.stop();
            center().sequencer.setTickPosition(0L);
            // TODO: rise event!
        }
    }

    public final void pause() {
        if (State.RUN == state()) {
            center().sequencer.stop();
            // TODO: rise event!
        }
    }

    public final void off() {
        if (center().sequencer.isOpen()) {
            center().sequencer.close();
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
