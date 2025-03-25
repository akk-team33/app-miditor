package de.team33.midix;

import de.team33.miditor.testing.AnyMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.sound.midi.MidiMessage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidiMetaMessageTypeTest {

    @ParameterizedTest
    @EnumSource
    void isTypeOf(final Case given) {
        Assertions.assertEquals(given.isTypeOf, given.type.isTypeOf(given.midiMessage));
    }

    @ParameterizedTest
    @EnumSource
    void isValid(final Case given) {
        Assertions.assertEquals(given.isValid, given.type.isValid(given.midiMessage));
    }

    @ParameterizedTest
    @EnumSource
    void valid(final Case given) {
        try {
            final MidiMessage result = given.type.valid(given.midiMessage);
            assertTrue(given.isValid, () -> "expected to fail - but was " + result);
        } catch (final IllegalArgumentException e) {
            // e.printStackTrace();
            assertFalse(given.isValid, () -> "NOT expected to fail - but was " + e);
            // TODO ...
            // assertTrue(e.getMessage().contains(given.type.name()), () -> e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    enum Case {

        TRACK_NAME(Midi.MetaMessage.Type.TRACK_NAME, true, true, 0xFF, 0x03, 3, 65, 66, 67),
        TRACK_NAME_TF_LONG(Midi.MetaMessage.Type.TRACK_NAME, true, false, 0xFF, 0x03, 3, 65, 66, 67, 68),
        TRACK_NAME_TF_SHORT(Midi.MetaMessage.Type.TRACK_NAME, true, false, 0xFF, 0x03, 3, 65, 66),
        EOF_TRACK(Midi.MetaMessage.Type.EOF_TRACK, true, true, 0xFF, 0x2F, 0),
        EOF_TRACK_TF_SHORT(Midi.MetaMessage.Type.EOF_TRACK, true, false, 0xFF, 0x2F),
        EOF_TRACK_FF_TYPE(Midi.MetaMessage.Type.EOF_TRACK, false, false, 0xFF, 0x2E, 0);

        private final Midi.MetaMessage.Type type;
        private final boolean isTypeOf;
        private final boolean isValid;
        private final MidiMessage midiMessage;

        Case(final Midi.MetaMessage.Type type, boolean isTypeOf, boolean isValid, final int... values) {
            this.type = type;
            this.isTypeOf = isTypeOf;
            this.isValid = isValid;
            this.midiMessage = AnyMessage.of(values);
        }
    }
}