package de.team33.midi;

import de.team33.midi.testing.MidiTestBase;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Track;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SequenceProxyTest extends MidiTestBase {

    private final SequenceProxy sequenceProxy;

    SequenceProxyTest() throws InvalidMidiDataException, IOException {
        this.sequenceProxy = new SequenceProxy(sequence());
    }

    @Test
    void tracks() {
        final List<Integer> expected = Stream.of(sequence().getTracks()).map(Track::size).toList();

        final List<MidiTrack> result = sequenceProxy.tracks();
        assertEquals(14, result.size());
        assertSame(result, sequenceProxy.tracks(), "two subsequent calls should return the same list");

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }
}