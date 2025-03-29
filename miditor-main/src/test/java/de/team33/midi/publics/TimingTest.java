package de.team33.midi.publics;

import de.team33.midix.TimeFormat;
import de.team33.midix.TimeStamp;
import de.team33.midix.Timing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimingTest {

    private final Timing timing = new Timing(6, 8, 32, 100000);

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
        final TimeStamp expected = new TimeStamp(2, 5, 2, 3, TimeFormat.of(timing).toString());
        final TimeStamp result = timing.timeStampOf(96 + (4 * 16) + 8 + 3);
        assertEquals(expected, result);
    }
}