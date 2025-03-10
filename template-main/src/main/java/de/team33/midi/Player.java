package de.team33.midi;

import de.team33.patterns.notes.alpha.Channel;

import java.util.function.Consumer;
import de.team33.midix.Timing;

public interface Player {

    Mode getMode(int index);

    Sequence getSequence();

    long getPosition();

    void setPosition(long ticks);

    State getState();

    void setState(State newState);

    int getTempo();

    void setTempo(int tempo);

    Timing getTiming();

    void setMode(int index, Mode newMode);

    void addListener(Event event, Consumer<? super Player> listener);

    enum Mode {
        NORMAL,
        SOLO,
        MUTE
    }

    enum State {
        IDLE,
        STOP,
        PAUSE,
        RUN
    }

    enum Event implements Channel<Player> {
        SetModes,
        SetPosition,
        SetState,
        SetTempo
    }
}
