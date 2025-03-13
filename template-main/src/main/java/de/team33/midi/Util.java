package de.team33.midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.team33.midix.Midi.MetaMessage.Type.SET_TEMPO;
import static de.team33.midix.Midi.MetaMessage.Type.TIME_SIGNATURE;

final class Util {

    static final double MSPMQN = 6.0E7; // microseconds per MIDI quarter-note

    private Util() {
    }

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

    static Optional<MidiEvent> firstTempoEvent(final Track track) {
        return stream(track).filter(event -> SET_TEMPO.isValid(event.getMessage()))
                            .findFirst();
    }

    static Optional<MidiEvent> firstTimeSignature(final Track track) {
        return stream(track).filter(event -> TIME_SIGNATURE.isValid(event.getMessage()))
                            .findFirst();
    }
}
