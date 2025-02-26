package de.team33.midi;

import de.team33.messaging.Registry;

public interface Player extends Registry<Player.Message> {
    Mode getMode(int var1);

    Sequence getSequence();

    long getPosition();

    State getState();

    int getTempo();

    Timing getTiming();

    void setMode(int var1, Mode var2);

    void setPosition(long var1);

    void setState(State var1);

    void setTempo(int var1);

    public interface Message extends de.team33.messaging.Message<Player> {
    }

    public static enum Mode {
        NORMAL,
        SOLO,
        MUTE;

        private Mode() {
        }
    }

    public interface SetModes extends Message {
    }

    public interface SetPosition extends Message {
    }

    public interface SetState extends Message {
    }

    public interface SetTempo extends Message {
    }

    public static enum State {
        IDLE,
        STOP,
        PAUSE,
        RUN;

        private State() {
        }
    }
}
