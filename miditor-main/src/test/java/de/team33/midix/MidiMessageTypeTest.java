package de.team33.midix;

import de.team33.miditor.testing.AnyMessage;
import de.team33.midix.Midi.Message.Type;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.sound.midi.MidiMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidiMessageTypeTest {

    @ParameterizedTest
    @EnumSource
    void isTypeOf(final Case given) {
        assertEquals(given.isTypeOf, given.type.isTypeOf(given.midiMessage));
    }

    @ParameterizedTest
    @EnumSource
    void isValid(final Case given) {
        assertEquals(given.isValid, given.type.isValid(given.midiMessage));
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
            assertTrue(e.getMessage().contains(given.type.name()));
        }
    }

    @SuppressWarnings("unused")
    enum Case {

        NOTE_OFF(Type.NOTE_OFF, true, true, 0x80, 63, 63),
        NOTE_OFF_FF(Type.NOTE_OFF, false, false, 0x90, 63, 63),
        NOTE_OFF_TF(Type.NOTE_OFF, true, false, 0x80, 63, 63, 63),
        NOTE_ON(Type.NOTE_ON, true, true, 0x90, 63, 63),
        NOTE_ON_FF(Type.NOTE_ON, false, false, 0x80, 63, 63),
        NOTE_ON_TF(Type.NOTE_ON, true, false, 0x90, 63),
        P_PRESS(Type.P_PRESS, true, true, 0xA0, 63, 63),
        CONTROL(Type.CONTROL, true, true, 0xB0, 63, 63),
        PROGRAM(Type.PROGRAM, true, true, 0xC0, 63),
        SYS_EX(Type.SYS_EX, true, true, 0xF0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        EOX(Type.EOX, true, true, 0xF7),
        META(Type.META, true, true, 0xFF, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        META_TF(Type.META, true, false, 0xFF);

        private final Type type;
        private final boolean isTypeOf;
        private final boolean isValid;
        private final MidiMessage midiMessage;

        Case(final Type type, boolean isTypeOf, boolean isValid, final int... values) {
            this.type = type;
            this.isTypeOf = isTypeOf;
            this.isValid = isValid;
            this.midiMessage = AnyMessage.of(values);
        }
    }
}