package de.team33.midi;

import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidiTrackTest {

    private final Sequence sequence;
    private final MidiTrack midiTrack;

    MidiTrackTest() throws InvalidMidiDataException {
        sequence = new Sequence(Sequence.PPQ, 96, 3);
        midiTrack = new MidiTrack(1, sequence.getTracks()[0]);
    }

    @SuppressWarnings("SameParameterValue")
    private static MidiMessage newShortMessage(final int status, final int b1, final int b2) {
        try {
            return new ShortMessage(status, b1, b2);
        } catch (final InvalidMidiDataException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static MidiEvent[] newEvents() {
        return IntStream.range(0x80, 0xA0)
                        .mapToObj(status -> new MidiEvent(newShortMessage(status, 64, 64),
                                                          status))
                        .toArray(MidiEvent[]::new);
    }

    @Test
    final void list() {
        final List<MidiEvent> expected = List.of(sequence.getTracks()[0].get(0));
        final List<MidiEvent> result = midiTrack.list();
        assertEquals(expected, result);
    }

    @Test
    final void midiChannels() {
        assertEquals(Set.of(), midiTrack.midiChannels());

        final Set<Integer> expected = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        midiTrack.add(newEvents());

        assertEquals(expected, midiTrack.midiChannels());
    }

    @Test
    final void name() throws InvalidMidiDataException {
        assertEquals("[undefined]", midiTrack.name());

        final String expected = "track 01";
        final byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
        midiTrack.add(new MidiEvent(new MetaMessage(0x03, bytes, 8), 0L));

        assertEquals(expected, midiTrack.name());
    }

    @Test
    final void add() throws InterruptedException {
        final MidiEvent[] events = newEvents();
        midiTrack.add(events);
        assertEquals(Arrays.asList(events), midiTrack.list().subList(0, events.length));
    }

    @Test
    final void setModified() throws InterruptedException {
        final AtomicBoolean setModified = new AtomicBoolean(false);
        final AtomicBoolean setEvents = new AtomicBoolean(false);
        final AtomicBoolean setChannels = new AtomicBoolean(false);
        final AtomicBoolean setName = new AtomicBoolean(false);
        final MidiEvent[] events = newEvents();
        midiTrack.add(MidiTrack.Channel.SetEvents, t -> setEvents.set(t.isModified()))
                 .add(MidiTrack.Channel.SetChannels, t -> setChannels.set(t.isModified()))
                 .add(MidiTrack.Channel.SetModified, t -> setModified.set(t.isModified()))
                 .add(MidiTrack.Channel.SetName, t -> setName.set(t.isModified()))
                 .add(events);

        Thread.sleep(5);
        assertTrue(setModified.get());
        assertTrue(setEvents.get());
        assertTrue(setChannels.get());
        assertTrue(setName.get());
    }

    @Test
    final void remove() {
        final List<MidiEvent> expected = List.of(sequence.getTracks()[0].get(0));
        final MidiEvent[] events = newEvents();
        midiTrack.add(events);
        midiTrack.remove(events);
        assertEquals(expected, midiTrack.list());
    }

    @Test
    final void isModified() {
        assertFalse(midiTrack.isModified());
        midiTrack.add(newEvents());
        assertTrue(midiTrack.isModified());
        midiTrack.resetModified();
        assertFalse(midiTrack.isModified());
    }

    @Test
    final void shift() {
        final MidiEvent[] events = newEvents();
        midiTrack.add(events)
                 .shift(10000);
        midiTrack.list()
                 .forEach(midiEvent -> assertTrue(10000 <= midiEvent.getTick()));
    }

    @Test
    final void extractChannels() {
        assertFalse(midiTrack.isModified());
        final Set<Integer> expected = midiTrack.add(newEvents())
                                               .midiChannels();
        final Map<Integer, List<MidiEvent>> result = midiTrack.extractChannels();
        assertEquals(expected, result.keySet());
        assertTrue(midiTrack.isModified());
    }
}