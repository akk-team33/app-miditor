package de.team33.miditor.backend.publics;

import de.team33.miditor.backend.TimeSignature;
import de.team33.miditor.backend.TimeStamp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeSignatureTest {

    private final TimeSignature timeSignature = new TimeSignature(6, 8, 32);

    @Test
    final void barNumerator() {
        assertEquals(6, timeSignature.barNumerator());
    }

    @Test
    final void barDenominator() {
        assertEquals(8, timeSignature.barDenominator());
    }

    @Test
    final void tickResolution() {
        assertEquals(32, timeSignature.tickResolution());
    }

    @Test
    final void subBeatDenominator() {
        assertEquals(16, timeSignature.subBeatDenominator());
    }

    @Test
    final void tickDenominator() {
        assertEquals(128, timeSignature.tickDenominator());
    }

    @Test
    final void barTicks() {
        assertEquals(96, timeSignature.barTicks());
    }

    @Test
    final void beatTicks() {
        assertEquals(16, timeSignature.beatTicks());
    }

    @Test
    final void subBeatTicks() {
        assertEquals(8, timeSignature.subBeatTicks());
    }

    @Test
    final void timeStampOf() {
        final TimeStamp expected = new TimeStamp(2, 5, 2, 3);
        final TimeStamp result = timeSignature.timeStampOf(96 + (4 * 16) + 8 + 3);
        assertEquals(expected, result);
    }
}