package de.team33.midi;

import de.team33.midi.testing.MidiTestBase;
import de.team33.midix.Timing;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
class FullScoreTest extends MidiTestBase {

    private final FullScore fullScore;

    FullScoreTest() throws InvalidMidiDataException, IOException {
        this.fullScore = new FullScore(sequence(), Runnable::run);
    }

    @Test
    final void getTracks() {
        final List<Integer> expected = Stream.of(sequence().getTracks()).map(Track::size).toList();

        final List<MidiTrack> result = fullScore.getTracks();
        assertEquals(14, result.size());
        assertSame(result, fullScore.getTracks(), "two subsequent calls should return the same list");

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_empty() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(1)).toList(); // 1 <-> EOT

        final List<MidiTrack> result = fullScore.create().getTracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_list() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(sequence().getTracks()[0]).map(Track::size)).toList();

        final List<MidiEvent> events = fullScore.getTracks().get(0).list();
        final List<MidiTrack> result = fullScore.create(events)
                                                .getTracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void delete_single() {
        delete(4, () -> fullScore.getTracks().stream().skip(4)
                                 .forEach(fullScore::delete));
    }

    @Test
    final void delete_array() {
        delete(5, () -> fullScore.delete(fullScore.getTracks().stream().skip(5).toArray(MidiTrack[]::new)));
    }

    @Test
    final void delete_list() {
        delete(6, () -> fullScore.delete(fullScore.getTracks().stream().skip(6).toList()));
    }

    private void delete(final int keep, final Runnable deletion) {
        final List<Integer> expected = Stream.of(sequence().getTracks()).limit(keep).map(Track::size).toList();

        deletion.run();

        final List<MidiTrack> result = fullScore.getTracks();
        assertEquals(keep, result.size());
        final List<Integer> sizes = result.stream().map(MidiTrack::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void join() {
        final int expected = Stream.of(sequence().getTracks())
                                   .mapToInt(Track::size).map(size -> size -1)
                                   .sum() + 1;
        fullScore.join(fullScore.getTracks());
        assertEquals(1, fullScore.getTracks().size());
        assertEquals(expected, fullScore.getTracks().get(0).size());
    }

    @Test
    final void split() {
        fullScore.join(fullScore.getTracks())
                 .split(fullScore.getTracks().get(0));
        assertEquals(13, fullScore.getTracks().size());
        assertEquals(List.of(29, 885, 2047, 817, 625, 1497, 155, 151, 71, 497, 2816, 87, 353),
                     fullScore.getTracks().stream().map(MidiTrack::size).toList());
    }

    @Test
    final void getTickLength() {
        assertEquals(sequence().getTickLength(), fullScore.getTickLength());
    }

    @Test
    final void getTempo() {
        assertEquals(116, fullScore.getTempo());
    }

    @Test
    final void getTiming() {
        assertEquals(new Timing(4, 4, 192, 59230), fullScore.getTiming());
    }

    @Test
    final void isModified() {
        assertFalse(fullScore.isModified());
        fullScore.delete(fullScore.getTracks().get(3));
        assertTrue(fullScore.isModified());
    }
}