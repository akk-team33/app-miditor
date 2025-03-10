package de.team33.midi;

import de.team33.midix.Midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class TrackProxy {

    private final int index;
    private final Track backing;
    private AtomicBoolean modification = new AtomicBoolean(false);
    private final Features features = new Features();

    public TrackProxy(final int index, final Track backing) {
        this.index = index;
        this.backing = backing;
    }

    private static boolean isChannelEvent(final MidiEvent midiEvent) {
        return isChannelStatus(midiEvent.getMessage().getStatus());
    }

    private static boolean isChannelStatus(final Integer status) {
        return (127 < status) && (status < 240);
    }

    private static int channelOf(final MidiEvent midiEvent) {
        return midiEvent.getMessage().getStatus() & 0x0f;
    }

    private TrackProxy postUpdate() {
        return postUpdate(true);
    }

    private TrackProxy postUpdate(final boolean modified) {
        modification.set(modified);
        features.reset();
        return this;
    }

    private Stream<MidiEvent> stream() {
        return IntStream.range(0, backing.size())
                        .mapToObj(backing::get);
    }

    public final List<MidiEvent> list() {
        return features.get(Key.LIST);
    }

    public final SortedSet<Integer> midiChannels() {
        return features.get(Key.MIDI_CHANNELS);
    }

    public final String name() {
        return features.get(Key.NAME);
    }

    public final TrackProxy add(final MidiEvent... events) {
        return add(Arrays.asList(events));
    }

    public final TrackProxy add(final Collection<? extends MidiEvent> events) {
        synchronized (backing) {
            events.forEach(backing::add);
            return postUpdate();
        }
    }

    public final TrackProxy remove(final MidiEvent... events) {
        return remove(Arrays.asList(events));
    }

    public final TrackProxy remove(final Collection<? extends MidiEvent> events) {
        synchronized (backing) {
            events.forEach(backing::remove);
            return postUpdate();
        }
    }

    public final MidiEvent get(final int index) {
        synchronized (backing) {
            return backing.get(index);
        }
    }

    public final int size() {
        synchronized (backing) {
            return backing.size();
        }
    }

    public final long ticks() {
        synchronized (backing) {
            return backing.ticks();
        }
    }

    @Deprecated // may stay as private (?)
    public final int index() {
        return index;
    }

    @Deprecated // may be better somewhere else (?)
    public final String getPrefix() {
        return String.format("Track %02d", index);
    }

    public final boolean isModified() {
        return modification.get();
    }

    final TrackProxy resetModified() {
        return postUpdate(false);
    }

    public final Map<Integer, List<MidiEvent>> extractChannels() {
        synchronized (backing) {
            final Map<Integer, List<MidiEvent>> result = stream().filter(TrackProxy::isChannelEvent)
                                                                 .collect(groupingBy(TrackProxy::channelOf));
            result.values().stream()
                  .flatMap(List::stream)
                  .forEach(backing::remove);
            postUpdate();
            return result;
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<TrackProxy, R> {

        Key<List<MidiEvent>> LIST = host -> host.features.newList();
        Key<SortedSet<Integer>> MIDI_CHANNELS = host -> host.features.newMidiChannels();
        Key<String> NAME = host -> host.features.newName();
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends de.team33.patterns.features.alpha.Features<TrackProxy> {

        private Features() {
            super(ConcurrentHashMap::new);
        }

        @Override
        protected final TrackProxy host() {
            return TrackProxy.this;
        }

        private List<MidiEvent> newList() {
            synchronized (backing) {
                return stream().toList();
            }
        }

        private SortedSet<Integer> newMidiChannels() {
            synchronized (backing) {
                final SortedSet<Integer> result =
                        stream().map(MidiEvent::getMessage)
                                .map(MidiMessage::getStatus)
                                .filter(status -> isChannelStatus(status)) // <-> isChannelMessage
                                .map(status -> status & 0x0f)
                                .collect(Collectors.toCollection(TreeSet::new));
                return Collections.unmodifiableSortedSet(result);
            }
        }

        private String newName() {
            synchronized (backing) {
                return stream().map(MidiEvent::getMessage)
                               .filter(Midi.MetaMessage.Type.TRACK_NAME::isValid)
                               .map(Midi.MetaMessage::trackName)
                               .findFirst()
                               .orElse("[undefined]");
            }
        }
    }
}
