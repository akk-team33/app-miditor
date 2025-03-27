package de.team33.miditor.model;

import de.team33.patterns.notes.beta.Channel;

import java.util.function.Consumer;

public interface TimeShift {

    int getDividend();

    void setDividend(int dividend);

    int getDivisor();

    void setDivisor(int divisor);

    int getNextDivisor();

    int getPrevDivisor();

    int getTicks();

    int getTickUnit();

    void addListener(Event event, Consumer<? super TimeShift> listener);

    enum Event implements Channel<TimeShift> {
        SetDividend,
        SetDivisor
    }
}
