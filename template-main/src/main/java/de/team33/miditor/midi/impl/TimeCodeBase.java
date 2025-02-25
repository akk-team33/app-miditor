package de.team33.miditor.midi.impl;

import net.team33.midi.TimeCode;
import net.team33.midi.Timing;

public abstract class TimeCodeBase implements TimeCode {
    private final long m_Ticks;

    public TimeCodeBase(long ticks) {
        this.m_Ticks = ticks;
    }

    public int getBar() {
        long ret = this.m_Ticks;
        ret /= (long)this.getTiming().getBarTicks();
        ++ret;
        return (int)ret;
    }

    public int getBeat() {
        long ret = this.m_Ticks;
        ret /= (long)this.getTiming().getBeatTicks();
        ret %= (long)this.getTiming().getBarBeats();
        ++ret;
        return (int)ret;
    }

    public int getSubBeat() {
        long ret = this.m_Ticks;
        int beatQuantization = this.getTiming().getSubBeatUnit() / this.getTiming().getBeatUnit();
        ret /= (long)this.getTiming().getSubBeatTicks();
        ret %= (long)beatQuantization;
        ++ret;
        return (int)ret;
    }

    public int getTicks() {
        return (int)(this.m_Ticks % (long)this.getTiming().getSubBeatTicks());
    }

    protected abstract Timing getTiming();

    public String toString() {
        String sQTicks = String.valueOf(this.getTiming().getSubBeatTicks());
        String fmtx = String.format("%%0%dd", sQTicks.length());
        return String.format("%04d:%d:%d:" + fmtx, this.getBar(), this.getBeat(), this.getSubBeat(), this.getTicks());
    }
}
