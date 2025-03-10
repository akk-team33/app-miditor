package de.team33.miditor.model;

import de.team33.midi.Timing;
import de.team33.messaging.Register;
import de.team33.messaging.sync.Router;
import de.team33.midi.util.TimingUtil;
import de.team33.midix.Timing;
import de.team33.patterns.notes.eris.Audience;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class TimeShiftBase implements TimeShift {

    private static final Set<Event> INITIAL_EVENTS = Set.of(Event.SetDivisor, Event.SetDividend);

    private final Audience audience = new Audience();
    private long dividend = getTiming().barNumerator();
    private long divisor = getTiming().barDenominator();
    private List<Integer> divisors = null;

    @Override
    public final void addListener(final Event event, final Consumer<? super TimeShift> listener) {
        audience.add(event, listener);
        if (INITIAL_EVENTS.contains(event)) {
            listener.accept(this);
        }
    }

    public final int getDividend() {
        return (int) dividend;
    }

    public final void setDividend(final int dividend) {
        if (this.dividend != dividend) {
            this.dividend = dividend;
            audience.send(Event.SetDividend, this);
        }
    }

    public final int getDivisor() {
        return (int) divisor;
    }

    public final void setDivisor(final int divisor) {
        if (getDivisors().contains(divisor)) {
            this.divisor = divisor;
            audience.send(Event.SetDivisor, this);
        }
    }

    private List<Integer> getDivisors() {
        if (null == divisors) {
            divisors = TimingUtil.getUnits(getTiming(), 1);
        }
        return divisors;
    }

    public final int getNextDivisor() {
        return getDivisors().get(getNextDivisorIndex());
    }

    private int getNextDivisorIndex() {
        final int ret = getDivisors().indexOf(getDivisor()) + 1;
        return ret == getDivisors().size() ? 0 : ret;
    }

    public final int getPrevDivisor() {
        return getDivisors().get(getPrevDivisorIndex());
    }

    private final int getPrevDivisorIndex() {
        final int ret = getDivisors().indexOf(getDivisor());
        return 0 == ret ? getDivisors().size() - 1 : ret - 1;
    }

    public final int getTicks() {
        return (int) (dividend * getTickUnit() / divisor);
    }

    public final int getTickUnit() {
        return getTiming().tickDenominator();
    }

    protected abstract Timing getTiming();
}
