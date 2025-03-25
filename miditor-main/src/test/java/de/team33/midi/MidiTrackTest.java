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

class MidiTrackTest extends MidiTestBase {

    private final Track rawTrack;
    private final MidiTrack midiTrack;
    private final TrackList trackList;

    MidiTrackTest() throws InvalidMidiDataException, IOException {
        trackList = new TrackList(sequence(), Runnable::run, this::onModifiedTrack);
        rawTrack = trackList.tracks().get(1);
        midiTrack = MidiTrack.factory(trackList)
                             .create(rawTrack);
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
        final List<MidiEvent> expected = Util.stream(rawTrack).toList();
        final List<MidiEvent> result = midiTrack.list();
        assertEquals(expected, result);
    }

    @Test
    final void midiChannels() {
        assertEquals(Set.of(0), midiTrack.midiChannels());

        final Set<Integer> expected = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        midiTrack.add(newEvents());

        assertEquals(expected, midiTrack.midiChannels());
    }

    @Test
    final void name() throws InvalidMidiDataException {
        Util.stream(rawTrack)
            .filter(event -> TRACK_NAME.isTypeOf(event.getMessage()))
            .toList()
            .forEach(rawTrack::remove);
        assertEquals("[undefined]", midiTrack.name());

        final String expected = "track 01";
        final byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
        midiTrack.add(new MidiEvent(TRACK_NAME.newMessage(bytes), 0L));

        assertEquals(expected, midiTrack.name());
    }

    @Test
    final void add() {
        final MidiEvent[] events = newEvents();
        midiTrack.remove(midiTrack.list())
                 .add(events);

        assertEquals(Arrays.asList(events), midiTrack.list().subList(0, events.length));
    }

    @Test
    final void setModified() throws InterruptedException {
        final Mutable<Boolean> setModified = new Mutable<>(null);
        final Mutable<List<MidiEvent>> setEvents = new Mutable<>(null);
        final Mutable<Set<Integer>> setChannels = new Mutable<>(null);
        final Mutable<String> setName = new Mutable<>(null);

        final MidiEvent[] events = newEvents();
        midiTrack.add(MidiTrack.Channel.SetEvents, t -> setEvents.set(t.list()))
                 .add(MidiTrack.Channel.SetChannels, t -> setChannels.set(t.midiChannels()))
                 .add(MidiTrack.Channel.SetModified, t -> setModified.set(t.isModified()))
                 .add(MidiTrack.Channel.SetName, t -> setName.set(t.name()))
                 .add(events);

        assertTrue(midiTrack.isModified());
        assertEquals(midiTrack.isModified(), setModified.get());
        assertEquals(midiTrack.list(), setEvents.get());
        assertEquals(midiTrack.midiChannels(), setChannels.get());
        assertEquals(midiTrack.name(), setName.get());

        setModified.set(null);
        setEvents.set(null);
        setName.set(null);
        setChannels.set(null);

        midiTrack.resetModified();

        assertFalse(midiTrack.isModified());
        assertEquals(midiTrack.isModified(), setModified.get());
        assertNull(setEvents.get());
        assertNull(setChannels.get());
        assertNull(setName.get());
    }

    @Test
    final void remove_array() {
        final List<MidiEvent> expected = List.of();
        final MidiEvent[] array = midiTrack.list().toArray(MidiEvent[]::new);
        midiTrack.remove(array);
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
        midiTrack.shift(10000);
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