package de.team33.miditor.backend;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;

// tickResolution // int getTickUnit() / 4
// barDenominator // int getBeatUnit();
// barNumerator   // int getBarBeats();
public record TimeSignature(int barNumerator, int barDenominator, int tickResolution) {

    private static final int U_MASK = 0xFF;

    static TimeSignature of(final MidiMessage validMessage, final Sequence sequence) {
        final byte[] bytes = validMessage.getMessage();
        return new TimeSignature(bytes[3] & U_MASK, (1 << (bytes[4] & U_MASK)), sequence.getResolution());
    }

    static TimeSignature of(final Sequence sequence) {
        return new TimeSignature(4, 4, sequence.getResolution());
    }

    // int getSubBeatUnit();
    public final int subBeatDenominator() {
        return Math.max(16, barDenominator << 1);
    }

    // int getBarTicks()
    public final int barTicks() {
        return barNumerator * beatTicks();
    }

    // int getBeatTicks()
    public final int beatTicks() {
        return (tickResolution << 2) / barDenominator;
    }

    // int getSubBeatTicks(); // -> xxx : tickResolution * 4 / subBeatDenominator // int getSubBeatTicks()
    public final int subBeatTicks() {
        return (tickResolution << 2) / subBeatDenominator();
    }

    // TODO: TimeCode getTimeCode(long var1);
}
