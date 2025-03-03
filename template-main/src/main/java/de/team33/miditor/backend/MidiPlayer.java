package de.team33.miditor.backend;

import de.team33.patterns.notes.alpha.Audience;

import javax.sound.midi.Sequencer;

import java.util.function.Consumer;

import static de.team33.miditor.backend.MidiCenter.CNV;

public class MidiPlayer {
    
    private final Audience audience;
    private final Sequencer sequencer;

    MidiPlayer(final Audience audience, final Sequencer sequencer) {
        this.audience = audience;
        this.sequencer = sequencer;
    }

    public final void addStateListener(final Consumer<State> listener) {
        audience.add(Channel.SET_STATE, listener);
        listener.accept(state());
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
            audience.send(Channel.SET_STATE, State.STOP);
        }
    }

    public final void start() {
        if (!sequencer.isRunning()) {
            on();
            sequencer.start();
            audience.send(Channel.SET_STATE, State.RUN);
        }
    }

    public final void stop() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            sequencer.setTickPosition(0L);
            audience.send(Channel.SET_STATE, State.STOP);
            audience.send(Channel.SET_POSITION, 0L);
        }
    }

    public final void pause() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            audience.send(Channel.SET_STATE, State.PAUSE);
        }
    }

    public final void off() {
        if (sequencer.isOpen()) {
            sequencer.close();
            audience.send(Channel.SET_STATE, State.IDLE);
        }
    }
}
