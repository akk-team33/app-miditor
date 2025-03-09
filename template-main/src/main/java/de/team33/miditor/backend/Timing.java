package de.team33.miditor.backend;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;

import static de.team33.miditor.backend.Util.unsigned;

public record Timing(int barNumerator,   // int getBarBeats();
                     int barDenominator, // int getBeatUnit();
                     int tickResolution) {

    // TODO: make package private!
    public static Timing of(final MidiMessage validMessage, final Sequence sequence) {
        final byte[] bytes = validMessage.getMessage();
        return new Timing(unsigned(bytes[3]), (1 << unsigned(bytes[4])), sequence.getResolution());
    }

    // TODO: make package private!
    public static Timing of(final Sequence sequence) {
        return new Timing(4, 4, sequence.getResolution());
    }

    // int getSubBeatUnit();
    public final int subBeatDenominator() {
        return Math.max(16, barDenominator << 1);
    }

    // int getTickUnit()
    public final int tickDenominator() {
        return tickResolution << 2;
    }

    // int getBarTicks()
    public final int barTicks() {
        return barNumerator * beatTicks();
    }

    // int getBeatTicks()
    public final int beatTicks() {
        return tickDenominator() / barDenominator;
    }

    // int getSubBeatTicks(); // -> xxx : tickResolution * 4 / subBeatDenominator // int getSubBeatTicks()
    public final int subBeatTicks() {
        return tickDenominator() / subBeatDenominator();
    }

    public final TimeStamp timeStampOf(final long tickPosition) {
        final int bar = (int) ((tickPosition / barTicks()) + 1);
        final int beat = (int) (((tickPosition / beatTicks()) % barNumerator) + 1);
        final int subBeat = (int) ((tickPosition / subBeatTicks()) % (subBeatDenominator() / barDenominator) + 1);
        final int moreTicks = (int) (tickPosition % subBeatTicks());
        return new TimeStamp(bar, beat, subBeat, moreTicks, subBeatTicks());
    }
}
