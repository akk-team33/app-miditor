package de.team33.miditor.model;

import de.team33.messaging.Registry;

public interface TimeShift extends Registry<TimeShift.Message> {
    int getDividend();

    int getDivisor();

    int getNextDivisor();

    int getPrevDivisor();

    int getTicks();

    int getTickUnit();

    void setDividend(int var1);

    void setDivisor(int var1);

    public interface Message extends de.team33.messaging.Message<TimeShift> {
    }

    public interface SetDividend extends Message {
    }

    public interface SetDivisor extends Message {
    }
}
