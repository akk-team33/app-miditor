package de.team33.midi;

public interface Timing {
    int getBarBeats();

    int getBarTicks();

    int getBeatTicks();

    int getBeatUnit();

    int getSubBeatTicks();

    int getSubBeatUnit();

    int getTickUnit();

    TimeStamp getTimeCode(long var1);
}
