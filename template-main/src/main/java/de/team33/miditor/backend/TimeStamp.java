package de.team33.miditor.backend;

public record TimeStamp(int bar, int beat, int subBeat, int moreTicks, String format) {

    @Override
    public final String toString() {
        return format.formatted(bar, beat, subBeat, moreTicks);
    }
}
