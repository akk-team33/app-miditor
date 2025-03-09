package de.team33.miditor.backend;

import de.team33.patterns.decision.carpo.Variety;

import javax.sound.midi.MidiMessage;
import java.util.Arrays;
import java.util.function.Predicate;

@SuppressWarnings({"InterfaceNeverImplemented", "MarkerInterface", "unused"})
interface Midi {

    interface Message {

        enum Group {

            CHANEL(0xF0),
            SYSTEM(0xFF);

            private final int statusMask;

            Group(final int statusMask) {
                this.statusMask = statusMask;
            }
        }

        enum Type {

            NOTE_OFF(Group.CHANEL, 0x80, 3),
            NOTE_ON(Group.CHANEL, 0x90, 3),
            P_PRESS(Group.CHANEL, 0xA0, 3),
            CONTROL(Group.CHANEL, 0xB0, 3),
            PROGRAM(Group.CHANEL, 0xC0, 2),
            C_PRESS(Group.CHANEL, 0xD0, 2),
            PITCH_BEND(Group.CHANEL, 0xE0, 3),
            SYS_EX(Group.SYSTEM, 0xF0, 3, Integer.MAX_VALUE),
            MTC(Group.SYSTEM, 0xF1, 2),
            SONG_PP(Group.SYSTEM, 0xF2, 3),
            SONG_SEL(Group.SYSTEM, 0xF3, 2),
            F4(Group.SYSTEM, 0xF4, 1, 3),
            F5(Group.SYSTEM, 0xF5, 1, 3),
            TUNE_REQ(Group.SYSTEM, 0xF6, 1),
            EOX(Group.SYSTEM, 0xF7, 1),
            CLOCK(Group.SYSTEM, 0xF8, 1),
            F9(Group.SYSTEM, 0xF9, 1, 3),
            START(Group.SYSTEM, 0xFA, 1),
            CONTINUE(Group.SYSTEM, 0xFB, 1),
            STOP(Group.SYSTEM, 0xFC, 1),
            FD(Group.SYSTEM, 0xFD, 1),
            META(Group.SYSTEM, 0xFF, 3, 130);

            private static final String ILLEGAL_MESSAGE = "Not a valid message of type %s:%n" +
                                                          "    status: %02Xh%n" +
                                                          "    length: %d%n" +
                                                          "    bytes:  %s%n";

            private final Predicate<byte[]> identificator;
            private final Variety<byte[]> validator;

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
            Type(final Group group, final int status, final int length) {
                this(group, status, length, length);
            }

            Type(final Group group, final int status, final int minLength, final int maxLength) {
                final Predicate<byte[]> isStatus = bytes -> (bytes[0] & group.statusMask) == status;
                final Predicate<byte[]> isLength;
                if (minLength == maxLength) {
                    isLength = bytes -> bytes.length == minLength;
                } else {
                    isLength = bytes -> (minLength <= bytes.length) && (bytes.length <= maxLength);
                }
                this.identificator = isStatus;
                this.validator = Variety.joined(isStatus, isLength);
            }

            final boolean isTypeOf(final MidiMessage message) {
                return identificator.test(message.getMessage());
            }

            final boolean isValid(final MidiMessage message) {
                return 0b11 == validator.apply(message.getMessage());
            }

            final MidiMessage valid(final MidiMessage message) {
                if (isValid(message)) {
                    return message;
                } else {
                    final byte[] bytes = message.getMessage();
                    final String text = ILLEGAL_MESSAGE.formatted(name(), bytes[0] & 0xff, bytes.length,
                                                                  Arrays.toString(bytes));
                    throw new IllegalArgumentException(text);
                }
            }
        }
    }

    interface MetaMessage {

        enum Type {

            TRACK_NAME(0x03),
            SET_TEMPO(0x51);

            private final int value;

            Type(final int value) {
                this.value = value;
            }

            final int value() {
                return value;
            }
        }
    }
}
