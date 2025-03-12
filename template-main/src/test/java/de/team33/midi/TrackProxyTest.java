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

class TrackProxyTest {

    private final Sequence sequence;
    private final TrackProxy trackProxy;

    TrackProxyTest() throws InvalidMidiDataException {
        sequence = new Sequence(Sequence.PPQ, 96, 3);
        trackProxy = new TrackProxy(1, sequence.getTracks()[0]);
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
        final List<MidiEvent> result = trackProxy.list();
        assertEquals(expected, result);
    }

    @Test
    final void midiChannels() {
        assertEquals(Set.of(), trackProxy.midiChannels());

        final Set<Integer> expected = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        trackProxy.add(newEvents());

        assertEquals(expected, trackProxy.midiChannels());
    }

    @Test
    final void name() throws InvalidMidiDataException {
        assertEquals("[undefined]", trackProxy.name());

        final String expected = "track 01";
        final byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
        trackProxy.add(new MidiEvent(new MetaMessage(0x03, bytes, 8), 0L));

        assertEquals(expected, trackProxy.name());
    }

    @Test
    final void add() throws InterruptedException {
        final MidiEvent[] events = newEvents();
        trackProxy.add(events);
        assertEquals(Arrays.asList(events), trackProxy.list().subList(0, events.length));
    }

    @Test
    final void setModified() throws InterruptedException {
        final AtomicBoolean setModified = new AtomicBoolean(false);
        final AtomicBoolean setEvents = new AtomicBoolean(false);
        final AtomicBoolean setChannels = new AtomicBoolean(false);
        final AtomicBoolean setName = new AtomicBoolean(false);
        final MidiEvent[] events = newEvents();
        trackProxy.add(TrackProxy.Channel.SetEvents, t -> setEvents.set(t.isModified()))
                  .add(TrackProxy.Channel.SetChannels, t -> setChannels.set(t.isModified()))
                  .add(TrackProxy.Channel.SetModified, t -> setModified.set(t.isModified()))
                  .add(TrackProxy.Channel.SetName, t -> setName.set(t.isModified()))
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
        trackProxy.add(events);
        trackProxy.remove(events);
        assertEquals(expected, trackProxy.list());
    }

    @Test
    final void isModified() {
        assertFalse(trackProxy.isModified());
        trackProxy.add(newEvents());
        assertTrue(trackProxy.isModified());
        trackProxy.resetModified();
        assertFalse(trackProxy.isModified());
    }

    @Test
    final void shift() {
        final MidiEvent[] events = newEvents();
        trackProxy.add(events)
                  .shift(10000);
        trackProxy.list()
                  .forEach(midiEvent -> assertTrue(10000 <= midiEvent.getTick()));
    }

    @Test
    final void extractChannels() {
        assertFalse(trackProxy.isModified());
        final Set<Integer> expected = trackProxy.add(newEvents())
                                                .midiChannels();
        final Map<Integer, List<MidiEvent>> result = trackProxy.extractChannels();
        assertEquals(expected, result.keySet());
        assertTrue(trackProxy.isModified());
    }
}