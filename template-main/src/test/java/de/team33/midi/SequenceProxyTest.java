package de.team33.midi;

import de.team33.midi.testing.MidiTestBase;
import de.team33.midix.Timing;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SequenceProxyTest extends MidiTestBase {

    private final SequenceProxy sequenceProxy;

    SequenceProxyTest() throws InvalidMidiDataException, IOException {
        this.sequenceProxy = new SequenceProxy(path(), sequence());
    }

    @Test
    final void load() throws Exception {
        final SequenceProxy other = SequenceProxy.load(sequenceProxy.getPath());
        assertEquals(sequenceProxy.getTracks().size(), other.getTracks().size());
    }

    @Test
    final void save() throws IOException {
        final long oldSize = Files.size(sequenceProxy.getPath());
        sequenceProxy.delete(sequenceProxy.getTracks().get(1));

        sequenceProxy.save();
        final long newSize = Files.size(sequenceProxy.getPath());

        assertTrue(oldSize > newSize);
    }

    @Test
    final void saveAs() throws IOException {
        final Path oldPath = sequenceProxy.getPath();
        final Path newPath = oldPath.getParent().resolve(UUID.randomUUID() + ".mid");
        assertFalse(Files.exists(newPath, LinkOption.NOFOLLOW_LINKS));

        sequenceProxy.saveAs(newPath);
        assertTrue(Files.exists(newPath, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    final void getTracks() {
        final List<Integer> expected = Stream.of(sequence().getTracks()).map(Track::size).toList();

        final List<MidiTrack> result = sequenceProxy.getTracks();
        assertEquals(14, result.size());
        assertSame(result, sequenceProxy.getTracks(), "two subsequent calls should return the same list");

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_empty() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(1)).toList(); // 1 <-> EOT

        final List<MidiTrack> result = sequenceProxy.create().getTracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_list() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(sequence().getTracks()[0]).map(Track::size)).toList();

        final List<MidiEvent> events = sequenceProxy.getTracks().get(0).list();
        final List<MidiTrack> result = sequenceProxy.create(events)
                                                    .getTracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void delete_single() {
        delete(4, () -> sequenceProxy.getTracks().stream().skip(4)
                                     .forEach(sequenceProxy::delete));
    }

    @Test
    final void delete_array() {
        delete(5, () -> sequenceProxy.delete(sequenceProxy.getTracks().stream().skip(5).toArray(MidiTrack[]::new)));
    }

    @Test
    final void delete_list() {
        delete(6, () -> sequenceProxy.delete(sequenceProxy.getTracks().stream().skip(6).toList()));
    }

    private void delete(final int keep, final Runnable deletion) {
        final List<Integer> expected = Stream.of(sequence().getTracks()).limit(keep).map(Track::size).toList();

        deletion.run();

        final List<MidiTrack> result = sequenceProxy.getTracks();
        assertEquals(keep, result.size());
        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void join() {
        final int expected = Stream.of(sequence().getTracks())
                                   .mapToInt(Track::size).map(size -> size -1)
                                   .sum() + 1;
        sequenceProxy.join(sequenceProxy.getTracks());
        assertEquals(1, sequenceProxy.getTracks().size());
        assertEquals(expected, sequenceProxy.getTracks().get(0).size());
    }

    @Test
    final void split() {
        sequenceProxy.join(sequenceProxy.getTracks())
                     .split(sequenceProxy.getTracks().get(0));
        assertEquals(13, sequenceProxy.getTracks().size());
        assertEquals(List.of(29, 885, 2047, 817, 625, 1497, 155, 151, 71, 497, 2816, 87, 353),
                     sequenceProxy.getTracks().stream().map(MidiTrack::size).toList());
    }

    @Test
    final void getTickLength() {
        assertEquals(sequence().getTickLength(), sequenceProxy.getTickLength());
    }

    @Test
    final void getTempo() {
        assertEquals(116, sequenceProxy.getTempo());
    }

    @Test
    final void getTiming() {
        assertEquals(new Timing(4, 4, 192, 59230), sequenceProxy.getTiming());
    }

    @Test
    final void isModified() {
        assertEquals(false, sequenceProxy.isModified());
        sequenceProxy.delete(sequenceProxy.getTracks().get(3));
        assertEquals(true, sequenceProxy.isModified());
    }
}