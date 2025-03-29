package de.team33.midi.util;

import de.team33.midi.Timing;

import java.util.List;
import java.util.stream.IntStream;

public final class TimingUtil {

    private TimingUtil() {
    }

    public static List<Integer> getUnits(final Timing timing, final int start) {
        return IntStream.range(Math.max(1, start), timing.tickDenominator())
                        .boxed()
                        .toList();
    }
}
