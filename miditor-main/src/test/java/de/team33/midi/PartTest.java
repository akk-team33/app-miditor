package de.team33.midi;

import de.team33.midi.testing.MidiTestBase;
import de.team33.patterns.mutable.alpha.Mutable;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static de.team33.midix.Midi.MetaMessage.Type.TRACK_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
class PartTest extends MidiTestBase {

    private final Track track;
    private final Part part;

    PartTest() throws InvalidMidiDataException, IOException {
        final TrackList trackList = new TrackList(sequence(), Runnable::run, this::onModifiedTrack);
        track = trackList.tracks().get(1);
        part = Part.factory(trackList)
                   .create(track);
    }

    private void onModifiedTrack() {
        // nothing to do
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
        final List<MidiEvent> expected = Util.stream(track).toList();
        final List<MidiEvent> result = part.list();
        assertEquals(expected, result);
    }

    @Test
    final void midiChannels() {
        assertEquals(Set.of(0), part.midiChannels());

        final Set<Integer> expected = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        part.add(newEvents());

        assertEquals(expected, part.midiChannels());
    }

    @Test
    final void name() {
        Util.stream(track)
            .filter(event -> TRACK_NAME.isTypeOf(event.getMessage()))
            .toList()
            .forEach(track::remove);
        assertEquals("[undefined]", part.name());

        final String expected = "track 01";
        final byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
        part.add(new MidiEvent(TRACK_NAME.newMessage(bytes), 0L));

        assertEquals(expected, part.name());
    }

    @Test
    final void add() {
        final MidiEvent[] events = newEvents();
        part.remove(part.list())
            .add(events);

        assertEquals(Arrays.asList(events), part.list().subList(0, events.length));
    }

    @Test
    final void setModified() {
        final Mutable<Boolean> setModified = new Mutable<>(null);
        final Mutable<List<MidiEvent>> setEvents = new Mutable<>(null);
        final Mutable<Set<Integer>> setChannels = new Mutable<>(null);
        final Mutable<String> setName = new Mutable<>(null);

        final MidiEvent[] events = newEvents();
        part.add(events)
            .registry()
            .add(Part.Channel.SetEvents, t -> setEvents.set(t.list()))
            .add(Part.Channel.SetChannels, t -> setChannels.set(t.midiChannels()))
            .add(Part.Channel.SetModified, t -> setModified.set(t.isModified()))
            .add(Part.Channel.SetName, t -> setName.set(t.name()));

        assertTrue(part.isModified());
        assertEquals(part.isModified(), setModified.get());
        assertEquals(part.list(), setEvents.get());
        assertEquals(part.midiChannels(), setChannels.get());
        assertEquals(part.name(), setName.get());

        setModified.set(null);
        setEvents.set(null);
        setName.set(null);
        setChannels.set(null);

        part.resetModified();

        assertFalse(part.isModified());
        assertEquals(part.isModified(), setModified.get());
        assertNull(setEvents.get());
        assertNull(setChannels.get());
        assertNull(setName.get());
    }

    @Test
    final void remove_array() {
        final List<MidiEvent> expected = List.of();
        final MidiEvent[] array = part.list().toArray(MidiEvent[]::new);
        part.remove(array);
        assertEquals(expected, part.list());
    }

    @Test
    final void isModified() {
        assertFalse(part.isModified());
        part.add(newEvents());
        assertTrue(part.isModified());
        part.resetModified();
        assertFalse(part.isModified());
    }

    @Test
    final void shift() {
        part.shift(10000);
        part.list()
            .forEach(midiEvent -> assertTrue(10000 <= midiEvent.getTick()));
    }

    @Test
    final void extractChannels() {
        assertFalse(part.isModified());
        final Set<Integer> expected = part.add(newEvents())
                                          .midiChannels();
        final Map<Integer, List<MidiEvent>> result = part.extractChannels();
        assertEquals(expected, result.keySet());
        assertTrue(part.isModified());
    }
}