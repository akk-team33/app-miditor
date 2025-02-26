package de.team33.midi;

import de.team33.messaging.Registry;

public interface Player extends Registry<Player.Message> {
    Mode getMode(int var1);

    Sequence getSequence();

    long getPosition();

    void setPosition(long var1);

    State getState();

    void setState(State var1);

    int getTempo();

    void setTempo(int var1);

    Timing getTiming();

    void setMode(int var1, Mode var2);

    public static enum Mode {
        NORMAL,
        SOLO,
        MUTE;

        private Mode() {
        }
    }

    public static enum State {
        IDLE,
        STOP,
        PAUSE,
        RUN;

        private State() {
        }
    }

    public interface Message extends de.team33.messaging.Message<Player> {
    }

    public interface SetModes extends Message {
    }

    public interface SetPosition extends Message {
    }

    public interface SetState extends Message {
    }

    public interface SetTempo extends Message {
    }
}
