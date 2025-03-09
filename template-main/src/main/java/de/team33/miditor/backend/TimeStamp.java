package de.team33.miditor.backend;

public record TimeStamp(int bar, int beat, int subBeat, int moreTicks, int subBeatTicks) {

    public final int moreTicksLength() {
        return String.valueOf(subBeatTicks).length();
    }

    @Override
    public final String toString() {
        final String format = "%%04d:%%d:%%d:%%0%dd".formatted(moreTicksLength());
        return format.formatted(bar, beat, subBeat, moreTicks);
    }
}
