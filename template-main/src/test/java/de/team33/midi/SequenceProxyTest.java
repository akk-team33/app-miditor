package de.team33.midi;

import de.team33.midi.testing.MidiTestBase;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
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
    final void tracks() {
        final List<Integer> expected = Stream.of(sequence().getTracks()).map(Track::size).toList();

        final List<MidiTrack> result = sequenceProxy.tracks();
        assertEquals(14, result.size());
        assertSame(result, sequenceProxy.tracks(), "two subsequent calls should return the same list");

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_empty() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(1)).toList(); // 1 <-> EOT

        final List<MidiTrack> result = sequenceProxy.create().tracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_list() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(sequence().getTracks()[0]).map(Track::size)).toList();

        final List<MidiEvent> events = sequenceProxy.tracks().get(0).list();
        final List<MidiTrack> result = sequenceProxy.create(events)
                                                    .tracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void delete_single() {
        delete(4, () -> sequenceProxy.tracks().stream().skip(4)
                                     .forEach(sequenceProxy::delete));
    }

    @Test
    final void delete_array() {
        delete(5, () -> sequenceProxy.delete(sequenceProxy.tracks().stream().skip(5).toArray(MidiTrack[]::new)));
    }

    @Test
    final void delete_list() {
        delete(6, () -> sequenceProxy.delete(sequenceProxy.tracks().stream().skip(6).toList()));
    }

    private void delete(final int keep, final Runnable deletion) {
        final List<Integer> expected = Stream.of(sequence().getTracks()).limit(keep).map(Track::size).toList();

        deletion.run();

        final List<MidiTrack> result = sequenceProxy.tracks();
        assertEquals(keep, result.size());
        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void getTickLength() {
        assertEquals(sequence().getTickLength(), sequenceProxy.getTickLength());
    }

    @Test
    final void getTempo() {
        assertEquals(116, sequenceProxy.getTempo());
    }
}