package de.team33.midi;

public interface TimeCode {
    int getBar();

    int getBeat();

    int getSubBeat();

    int getTicks();

    String toString();
}
