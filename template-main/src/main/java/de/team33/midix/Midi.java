package de.team33.midix;

import de.team33.patterns.decision.carpo.Variety;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

@SuppressWarnings({"InterfaceNeverImplemented", "MarkerInterface", "unused"})
public interface Midi {

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

            private final int status;
            private final Predicate<byte[]> identificator;
            private final Variety<byte[]> validator;
            private final IntUnaryOperator combiner;

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
            Type(final Group group, final int status, final int length) {
                this(group, status, length, length);
            }

            Type(final Group group, final int status, final int minLength, final int maxLength) {
                this.status = status;
                final Predicate<byte[]> isStatus = bytes -> (bytes[0] & group.statusMask) == status;
                final Predicate<byte[]> isLength;
                if (minLength == maxLength) {
                    isLength = bytes -> bytes.length == minLength;
                } else {
                    isLength = bytes -> {
                        final int length = bytes.length;
                        return (minLength <= length) && (length <= maxLength);
                    };
                }
                this.identificator = isStatus;
                this.validator = Variety.joined(isStatus, isLength);
                this.combiner = (Group.CHANEL == group)
                        ? (midiChannel -> status | midiChannel)
                        : (midiChannel -> status);
            }

            public final boolean isTypeOf(final MidiMessage message) {
                return identificator.test(message.getMessage());
            }

            public final boolean isValid(final MidiMessage message) {
                return 0b11 == validator.apply(message.getMessage());
            }

            public final <M extends MidiMessage> M valid(final M message) {
                if (isValid(message)) {
                    return message;
                } else {
                    final byte[] bytes = message.getMessage();
                    final String text = ILLEGAL_MESSAGE.formatted(name(), bytes[0] & 0xff, bytes.length,
                                                                  Arrays.toString(bytes));
                    throw new IllegalArgumentException(text);
                }
            }

            public ShortMessage newChnMessage(final int channel, final int p1) {
                return newChnMessage(channel, p1, 0);
            }

            public ShortMessage newChnMessage(final int channel, final int p1, final int p2) {
                return valid(Util.CNV.get(() -> new ShortMessage(combiner.applyAsInt(channel), p1, p2)));
            }

            public ShortMessage newSysMessage() {
                return newSysMessage(0, 0);
            }

            public ShortMessage newSysMessage(final int p1) {
                return newSysMessage(p1, 0);
            }

            public ShortMessage newSysMessage(final int p1, final int p2) {
                return valid(Util.CNV.get(() -> new ShortMessage(status, p1, p2)));
            }
        }
    }

    interface MetaMessage {

        static String trackName(final MidiMessage validMessage) {
            final byte[] bytes = validMessage.getMessage();
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }

        enum Type {

            TRACK_NAME(0x03, 0, 127),
            EOF_TRACK(0x2F, 0),
            SET_TEMPO(0x51, 3),
            TIME_SIGNATURE(0x58, 4);

            private static final String ILLEGAL_MESSAGE = "Not a valid meta message of type %s:%n" +
                                                          "    status: %02Xh%n" +
                                                          "    type: %02Xh%n" +
                                                          "    length: %d%n" +
                                                          "    bytes:  %s%n";

            private final int type;
            private final Predicate<byte[]> identificator;
            private final Variety<byte[]> validator;

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
            Type(final int type, final int length) {
                this(type, length, length);
            }

            Type(final int type, final int minLength, final int maxLength) {
                this.type = type;
                final Predicate<byte[]> isType = bytes -> (bytes[1] & 0xFF) == type;
                final Predicate<byte[]> isLength;
                if (minLength == maxLength) {
                    isLength = bytes -> (bytes[2] & 0xFF) == minLength;
                } else {
                    isLength = bytes -> {
                        final int length = bytes[2] & 0xFF;
                        return (minLength <= length) && (length <= maxLength);
                    };
                }
                final Predicate<byte[]> isBytesLength = bytes -> bytes.length == (bytes[2] & 0xFF) + 3;
                this.identificator = isType;
                this.validator = Variety.joined(isType, isLength, isBytesLength);
            }

            public final boolean isTypeOf(final MidiMessage message) {
                return Message.Type.META.isTypeOf(message) && identificator.test(message.getMessage());
            }

            public final boolean isValid(final MidiMessage message) {
                return Message.Type.META.isValid(message) && isValidContent(message);
            }

            private boolean isValidContent(final MidiMessage metaMessage) {
                return 0b111 == validator.apply(metaMessage.getMessage());
            }

            public <M extends MidiMessage> M valid(final M message) {
                if (isValidContent(Message.Type.META.valid(message))) {
                    return message;
                } else {
                    final byte[] bytes = message.getMessage();
                    final String text = ILLEGAL_MESSAGE.formatted(name(), bytes[0] & 0xff,
                                                                  bytes[1] & 0xff, bytes[2] & 0xff,
                                                                  Arrays.toString(bytes));
                    throw new IllegalArgumentException(text);
                }
            }

            public javax.sound.midi.MetaMessage newMessage(final byte[] bytes) {
                return valid(Util.CNV.get(() -> new javax.sound.midi.MetaMessage(type, bytes, bytes.length)));
            }
        }
    }
}
