package de.team33.miditor.backend;

public record TimeFormat(String bar, String beat, String subBeat, String moreTicks) {

    private static final String FORMAT = "%%0%dd";

    public static TimeFormat of(final Timing timing) {
        return new TimeFormat(FORMAT.formatted(String.valueOf(timing.tickLength() / timing.barTicks())
                                                     .length()),
                              FORMAT.formatted(String.valueOf(timing.barTicks() / timing.beatTicks())
                                                     .length()),
                              FORMAT.formatted(String.valueOf(timing.beatTicks() / timing.subBeatTicks())
                                                     .length()),
                              FORMAT.formatted(String.valueOf(timing.subBeatTicks())
                                                     .length()));
    }

    @Override
    public String toString() {
        return "%s:%s:%s:%s".formatted(bar, beat, subBeat, moreTicks);
    }
}
