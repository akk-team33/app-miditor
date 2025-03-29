package de.team33.midi;

import de.team33.midi.testing.MidiTestBase;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreTest extends MidiTestBase {

    private final Score score;

    ScoreTest() throws InvalidMidiDataException, IOException {
        this.score = new Score(sequence(), Runnable::run);
    }

    @Test
    final void getTracks() {
        final List<Integer> expected = Stream.of(sequence().getTracks()).map(Track::size).toList();

        final List<Part> result = score.getTracks();
        assertEquals(14, result.size());
        assertSame(result, score.getTracks(), "two subsequent calls should return the same list");

        final List<Integer> sizes = result.stream().map(Part::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_empty() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(1)).toList(); // 1 <-> EOT

        final List<Part> result = score.create(Collections.emptyList()).getTracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(Part::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void create_list() {
        final List<Integer> expected = Stream.concat(
                Stream.of(sequence().getTracks()).map(Track::size),
                Stream.of(sequence().getTracks()[0]).map(Track::size)).toList();

        final List<MidiEvent> events = score.getTracks().get(0).list();
        final List<Part> result = score.create(events)
                                       .getTracks();
        assertEquals(15, result.size());

        final List<Integer> sizes = result.stream().map(Part::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void delete_list() {
        final List<Integer> expected = Stream.of(sequence().getTracks())
                                             .limit(6)
                                             .map(Track::size)
                                             .toList();

        score.delete(score.getTracks().stream().skip(6).toList());

        final List<Part> result = score.getTracks();
        assertEquals(6, result.size());
        final List<Integer> sizes = result.stream().map(Part::size).toList();
        assertEquals(expected, sizes);
    }

    @Test
    final void join() {
        final int expected = Stream.of(sequence().getTracks())
                                   .mapToInt(Track::size).map(size -> size -1)
                                   .sum() + 1;
        score.join(score.getTracks());
        assertEquals(1, score.getTracks().size());
        assertEquals(expected, score.getTracks().get(0).size());
    }

    @Test
    final void split() {
        score.join(score.getTracks())
             .split(score.getTracks().get(0));
        assertEquals(13, score.getTracks().size());
        assertEquals(List.of(29, 885, 2047, 817, 625, 1497, 155, 151, 71, 497, 2816, 87, 353),
                     score.getTracks().stream().map(Part::size).toList());
    }

    @Test
    final void getTickLength() {
        assertEquals(sequence().getTickLength(), score.getTickLength());
    }

    @Test
    final void getTempo() {
        assertEquals(116, score.getTempo());
    }

    @Test
    final void getTiming() {
        assertEquals(new Timing(4, 4, 192, 59230), score.getTiming());
    }

    @Test
    final void isModified() {
        assertFalse(score.isModified());
        score.delete(List.of(score.getTracks().get(3)));
        assertTrue(score.isModified());
    }
}