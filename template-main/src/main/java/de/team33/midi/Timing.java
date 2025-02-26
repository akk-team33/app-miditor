package de.team33.midi;

public interface Timing {
    int getBarBeats();

    int getBarTicks();

    int getBeatTicks();

    int getBeatUnit();

    int getSubBeatTicks();

    int getSubBeatUnit();

    int getTickUnit();

    TimeCode getTimeCode(long var1);
}
