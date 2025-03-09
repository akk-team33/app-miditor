package de.team33.midi;

public interface TimeStamp {

    int getBar();

    int getBeat();

    int getSubBeat();

    int getTicks();

    String toString();
}
