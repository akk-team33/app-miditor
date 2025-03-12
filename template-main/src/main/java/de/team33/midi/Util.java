package de.team33.midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Util {

    private Util() {}

    static Stream<MidiEvent> stream(final Track track) {
        return IntStream.range(0, track.size())
                        .mapToObj(track::get);
    }

    static Stream<MidiEvent> stream(final MidiTrack track) {
        return stream(track.backing());
    }

    static Stream<Track> stream(final Sequence sequence) {
        return Stream.of(sequence.getTracks());
    }
}
