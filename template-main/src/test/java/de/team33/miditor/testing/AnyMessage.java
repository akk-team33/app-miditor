package de.team33.miditor.testing;

import javax.sound.midi.MidiMessage;
import java.util.stream.IntStream;

@SuppressWarnings("UseOfClone")
public final class AnyMessage extends MidiMessage {

    private AnyMessage(final byte[] data) {
        super(data);
    }

    public static AnyMessage of(final int... ints) {
        final byte[] bytes = new byte[ints.length];
        IntStream.range(0, ints.length).forEach(index -> bytes[index] = (byte) ints[index]);
        return new AnyMessage(bytes);
    }

    @Override
    public final AnyMessage clone() {
        return new AnyMessage(getMessage());
    }
}
