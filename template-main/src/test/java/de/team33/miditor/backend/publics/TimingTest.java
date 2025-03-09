package de.team33.miditor.backend.publics;

import de.team33.miditor.backend.Timing;
import de.team33.miditor.backend.TimeStamp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimingTest {

    private final Timing timing = new Timing(6, 8, 32);

    @Test
    final void barNumerator() {
        assertEquals(6, timing.barNumerator());
    }

    @Test
    final void barDenominator() {
        assertEquals(8, timing.barDenominator());
    }

    @Test
    final void tickResolution() {
        assertEquals(32, timing.tickResolution());
    }

    @Test
    final void subBeatDenominator() {
        assertEquals(16, timing.subBeatDenominator());
    }

    @Test
    final void tickDenominator() {
        assertEquals(128, timing.tickDenominator());
    }

    @Test
    final void barTicks() {
        assertEquals(96, timing.barTicks());
    }

    @Test
    final void beatTicks() {
        assertEquals(16, timing.beatTicks());
    }

    @Test
    final void subBeatTicks() {
        assertEquals(8, timing.subBeatTicks());
    }

    @Test
    final void timeStampOf() {
        final TimeStamp expected = new TimeStamp(2, 5, 2, 3);
        final TimeStamp result = timing.timeStampOf(96 + (4 * 16) + 8 + 3);
        assertEquals(expected, result);
    }
}