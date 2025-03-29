package de.team33.midi.util;

import de.team33.midi.Timing;

import java.util.List;
import java.util.Vector;

public final class TimingUtil {

    private TimingUtil() {
    }

    public static List<Integer> getUnits(final Timing timing, final int start) {
        final List<Integer> ret = new Vector();
        int i = start < 1 ? 1 : start;

        for (final int n = timing.tickDenominator(); i <= n; ++i) {
            if (n % i == 0) {
                ret.add(i);
            }
        }

        return ret;
    }
}
