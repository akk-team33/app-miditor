package de.team33.midi;

import de.team33.midix.Timing;
import de.team33.patterns.notes.alpha.Channel;

import java.util.function.Consumer;

public interface MidiPlayer {

    Mode getMode(int index);

    MidiSequence getSequence();

    long getPosition();

    void setPosition(long ticks);

    State getState();

    void setState(State newState);

    int getTempo();

    void setTempo(int tempo);

    Timing getTiming();

    void setMode(int index, Mode newMode);

    void addListener(Event event, Consumer<? super MidiPlayer> listener);

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

    enum Event implements Channel<MidiPlayer> {
        SetModes,
        SetPosition,
        SetState,
        SetTempo
    }
}
