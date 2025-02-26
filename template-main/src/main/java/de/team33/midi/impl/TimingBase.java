package de.team33.midi.impl;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import de.team33.midi.TimeCode;
import de.team33.midi.Timing;

public abstract class TimingBase implements Timing {
    private int m_BeatUnit = 4;
    private int m_TimeBeats = 4;
    private int m_SubBeatUnit = 16;

    protected TimingBase(MidiEvent timing) {
        if (timing != null) {
            MidiMessage mm = timing.getMessage();
            byte[] b = mm.getMessage();
            if (b.length > 6) {
                this.m_TimeBeats = b[3] & 255;
                this.m_BeatUnit = 1 << (b[4] & 255);
                if (this.m_SubBeatUnit < this.m_BeatUnit) {
                    this.m_SubBeatUnit = this.m_BeatUnit;
                }
            }
        }

    }

    public int getBeatTicks() {
        return this.getTickUnit() / this.getBeatUnit();
    }

    public int getBeatUnit() {
        return this.m_BeatUnit;
    }

    protected abstract Sequence getSequence();

    public int getSubBeatTicks() {
        return this.getSequence().getResolution() * 4 / this.getSubBeatUnit();
    }

    public int getSubBeatUnit() {
        return this.m_SubBeatUnit;
    }

    public int getTickUnit() {
        return this.getSequence().getResolution() * 4;
    }

    public int getBarBeats() {
        return this.m_TimeBeats;
    }

    public TimeCode getTimeCode(long ticks) {
        return new TIME_STAMP(ticks);
    }

    public int getBarTicks() {
        return this.getBeatTicks() * this.getBarBeats();
    }

    private class TIME_STAMP extends TimeCodeBase {
        public TIME_STAMP(long ticks) {
            super(ticks);
        }

        protected Timing getTiming() {
            return TimingBase.this;
        }
    }
}
