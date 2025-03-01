package de.team33.midi.util;

import javax.sound.midi.MidiMessage;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

public class MidiMessageUtil {

    private static final int CMS_MIN = 128;
    private static final int SMS_MIN = 240;
    private static final int META_STATUS = 255;

    public static String trackName(final MidiMessage message) {
        return trackName(message.getMessage());
    }

    private static String trackName(final byte[] bytes) {
        return new String(bytes, 3, bytes.length - 3, StandardCharsets.US_ASCII);
    }

    public static boolean isTrackName(final MidiMessage message) {
        return isMetaMessage(message) && isTrackName(message.getMessage());
    }

    private static boolean isTrackName(final byte[] bytes) {
        return (2 < bytes.length) && (3 == bytes[1]) && (bytes.length - 3 == bytes[2]);
    }

    public static boolean isMetaMessage(final MidiMessage message) {
        return META_STATUS == message.getStatus();
    }

    public static boolean isChannelMessage(final MidiMessage message) {
        return isChannelMessage(message.getStatus());
    }

    private static boolean isChannelMessage(final int status) {
        return (CMS_MIN <= status) && (SMS_MIN > status);
    }
}
