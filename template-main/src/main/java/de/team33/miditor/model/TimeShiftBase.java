package de.team33.miditor.model;

import de.team33.messaging.Register;
import de.team33.messaging.sync.Router;
import de.team33.midi.util.TimingUtil;
import de.team33.midix.Timing;

import java.util.Arrays;
import java.util.List;

public abstract class TimeShiftBase implements TimeShift {
    private final SET_DIVIDEND msgSetDividend = new SET_DIVIDEND();
    private final SET_DIVISOR msgSetDivisor = new SET_DIVISOR();
    private final Router<TimeShift.Message> router = new Router();
    private long dividend = (long) this.getTiming().barNumerator();
    private long divisor = (long) this.getTiming().barDenominator();
    private List<Integer> divisors = null;

    public TimeShiftBase() {
        this.router.addInitials(Arrays.asList(this.msgSetDividend, this.msgSetDivisor));
    }

    public int getDividend() {
        return (int) this.dividend;
    }

    public final void setDividend(int value) {
        if (this.dividend != (long) value) {
            this.dividend = (long) value;
            this.router.accept(this.msgSetDividend);
        }

    }

    public int getDivisor() {
        return (int) this.divisor;
    }

    public final void setDivisor(int value) {
        if (this.getDivisors().contains(value)) {
            this.divisor = (long) value;
            this.router.accept(this.msgSetDivisor);
        }

    }

    private List<Integer> getDivisors() {
        if (this.divisors == null) {
            this.divisors = TimingUtil.getUnits(this.getTiming(), 1);
        }

        return this.divisors;
    }

    public int getNextDivisor() {
        return (Integer) this.getDivisors().get(this.getNextDivisorIndex());
    }

    private int getNextDivisorIndex() {
        int ret = this.getDivisors().indexOf(this.getDivisor()) + 1;
        return ret == this.getDivisors().size() ? 0 : ret;
    }

    public final int getPrevDivisor() {
        return (Integer) this.getDivisors().get(this.getPrevDivisorIndex());
    }

    private final int getPrevDivisorIndex() {
        int ret = this.getDivisors().indexOf(this.getDivisor());
        return ret == 0 ? this.getDivisors().size() - 1 : ret - 1;
    }

    public <MSX extends TimeShift.Message> Register<MSX> getRegister(Class<MSX> msgClass) {
        return this.router.getRegister(msgClass);
    }

    public final int getTicks() {
        return (int) (this.dividend * (long) this.getTickUnit() / this.divisor);
    }

    public final int getTickUnit() {
        return this.getTiming().tickDenominator();
    }

    protected abstract Timing getTiming();

    private class MESSAGE implements TimeShift.Message {
        private MESSAGE() {
        }

        public TimeShift getSender() {
            return TimeShiftBase.this;
        }
    }

    private class SET_DIVIDEND extends MESSAGE implements TimeShift.SetDividend {
        private SET_DIVIDEND() {
            super();
        }
    }

    private class SET_DIVISOR extends MESSAGE implements TimeShift.SetDivisor {
        private SET_DIVISOR() {
            super();
        }
    }
}
