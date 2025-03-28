package de.team33.midi.util;

import de.team33.midi.Part;

import javax.sound.midi.MidiEvent;
import java.util.List;
import java.util.stream.IntStream;

public final class TrackUtil {

    private TrackUtil() {
    }

    private static List<MidiEvent> getEvents(final Part track, final int[] selection) {
        return IntStream.of(selection)
                        .filter(index -> (0 <= index) && (index < track.size()))
                        .mapToObj(track::get)
                        .toList();
    }

    public static void remove(final Part track, final int[] selection) {
        track.remove(getEvents(track, selection));
    }
}
