package de.team33.midi.util;

import de.team33.miditor.backend.Timing;

import java.util.List;
import java.util.Vector;

public abstract class TimingUtil {
    public TimingUtil() {
    }

    public static List<Integer> getUnits(Timing timing, int start) {
        List<Integer> ret = new Vector();
        int i = start < 1 ? 1 : start;

        for (int n = timing.tickDenominator(); i <= n; ++i) {
            if (n % i == 0) {
                ret.add(i);
            }
        }

        return ret;
    }
}
