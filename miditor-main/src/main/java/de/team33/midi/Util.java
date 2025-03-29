package de.team33.midi;

import de.team33.patterns.decision.carpo.Variety;
import de.team33.patterns.exceptional.dione.Converter;
import de.team33.patterns.exceptional.dione.Wrapping;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.team33.midi.Util.MetaMessage.Type.SET_TEMPO;
import static de.team33.midi.Util.MetaMessage.Type.TIME_SIGNATURE;

final class Util {

    static final Converter CNV = Converter.using(Wrapping.method(IllegalStateException::new));
    static final double MICROSECONDS_PER_MINUTE = 6.0E7;

    private static final int U_MASK = 0xff;

    private Util() {
    }

    static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    static Stream<MidiEvent> stream(final Track track) {
        return IntStream.range(0, track.size())
                        .mapToObj(track::get);
    }

    static Stream<MidiEvent> stream(final Part track) {
        return stream(track.backing());
    }

    static Stream<Track> stream(final Sequence sequence) {
        return Stream.of(sequence.getTracks());
    }

    static Optional<MidiEvent> firstTempoEvent(final Track track) {
        return stream(track).filter(event -> SET_TEMPO.isValid(event.getMessage()))
                            .findFirst();
    }

    static Optional<MidiEvent> firstTimeSignature(final Track track) {
        return stream(track).filter(event -> TIME_SIGNATURE.isValid(event.getMessage()))
                            .findFirst();
    }

    static int tracksSize(final Sequencer sequencer) {
        return Optional.ofNullable(sequencer.getSequence())
                       .map(Util::tracksSize)
                       .orElse(0);
    }

    private static int tracksSize(final Sequence sequence) {
        return sequence.getTracks().length;
    }

    static int unsigned(final byte value) {
        return value & U_MASK;
    }

    static final class Message {

        private Message() {
        }

        enum Group {

            CHANEL(0xF0),
            SYSTEM(0xFF);

            private final int statusMask;

            Group(final int statusMask) {
                this.statusMask = statusMask;
            }
        }

        @SuppressWarnings("unused")
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

            final boolean isTypeOf(final MidiMessage message) {
                return identificator.test(message.getMessage());
            }

            final boolean isValid(final MidiMessage message) {
                return 0b11 == validator.apply(message.getMessage());
            }

            final <M extends MidiMessage> M valid(final M message) {
                if (isValid(message)) {
                    return message;
                } else {
                    final byte[] bytes = message.getMessage();
                    final String text = ILLEGAL_MESSAGE.formatted(name(), bytes[0] & 0xff, bytes.length,
                                                                  Arrays.toString(bytes));
                    throw new IllegalArgumentException(text);
                }
            }

            final ShortMessage newChnMessage(final int channel, final int p1) {
                return newChnMessage(channel, p1, 0);
            }

            final ShortMessage newChnMessage(final int channel, final int p1, final int p2) {
                return valid(CNV.get(() -> new ShortMessage(combiner.applyAsInt(channel), p1, p2)));
            }

            final ShortMessage newSysMessage() {
                return newSysMessage(0, 0);
            }

            final ShortMessage newSysMessage(final int p1) {
                return newSysMessage(p1, 0);
            }

            @SuppressWarnings("SameParameterValue")
            final ShortMessage newSysMessage(final int p1, final int p2) {
                return valid(CNV.get(() -> new ShortMessage(status, p1, p2)));
            }
        }
    }

    static final class MetaMessage {

        private MetaMessage() {
        }

        static String trackName(final MidiMessage validMessage) {
            final byte[] bytes = validMessage.getMessage();
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }

        @SuppressWarnings("unused")
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

            final boolean isTypeOf(final MidiEvent event) {
                return isTypeOf(event.getMessage());
            }

            final boolean isTypeOf(final MidiMessage message) {
                return Message.Type.META.isTypeOf(message) && identificator.test(message.getMessage());
            }

            final boolean isValid(final MidiEvent event) {
                return isValid(event.getMessage());
            }

            final boolean isValid(final MidiMessage message) {
                return Message.Type.META.isValid(message) && isValidContent(message);
            }

            private boolean isValidContent(final MidiMessage metaMessage) {
                return 0b111 == validator.apply(metaMessage.getMessage());
            }

            final <M extends MidiMessage> M valid(final M message) {
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

            final javax.sound.midi.MetaMessage newMessage(final byte[] bytes) {
                return valid(CNV.get(() -> new javax.sound.midi.MetaMessage(type, bytes, bytes.length)));
            }
        }
    }
}
