package de.team33.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;

public record Timing(int barNumerator,
                     int barDenominator,
                     int tickResolution,
                     long tickLength) {

    static Timing of(final MidiMessage validMessage, final Sequence sequence) {
        final byte[] bytes = validMessage.getMessage();
        return new Timing(Util.unsigned(bytes[3]),
                          (1 << Util.unsigned(bytes[4])),
                          sequence.getResolution(),
                          sequence.getTickLength());
    }

    static Timing of(final Sequence sequence) {
        return new Timing(4, 4, sequence.getResolution(), sequence.getTickLength());
    }

    public final int subBeatDenominator() {
        //noinspection MagicNumber
        return Math.max(16, barDenominator << 1);
    }

    public final int tickDenominator() {
        return tickResolution << 2;
    }

    public final int barTicks() {
        return barNumerator * beatTicks();
    }

    public final int beatTicks() {
        return tickDenominator() / barDenominator;
    }

    public final int subBeatTicks() {
        return tickDenominator() / subBeatDenominator();
    }

    public final TimeStamp timeStampOf(final long tickPosition) {
        //noinspection NumericCastThatLosesPrecision
        final int bar = (int) ((tickPosition / barTicks()) + 1);
        //noinspection NumericCastThatLosesPrecision
        final int beat = (int) (((tickPosition / beatTicks()) % barNumerator) + 1);
        //noinspection NumericCastThatLosesPrecision
        final int subBeat = (int) ((tickPosition / subBeatTicks()) % (subBeatDenominator() / barDenominator) + 1);
        final int moreTicks = (int) (tickPosition % subBeatTicks());
        return new TimeStamp(bar, beat, subBeat, moreTicks, TimeFormat.of(this).toString());
    }
}
