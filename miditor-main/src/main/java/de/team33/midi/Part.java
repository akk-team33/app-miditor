package de.team33.midi;

import de.team33.patterns.lazy.narvi.LazyFeatures;
import de.team33.patterns.notes.beta.Sender;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings({"ClassNamePrefixedWithPackageName", "ClassWithTooManyMethods"})
public final class Part extends Sender<Part> {

    private final Parts parts;
    private final Track track;
    private final AtomicLong modCounter;
    private final Features features = new Features();

    private Part(final Parts parts, final Parts.Entry entry) {
        super(Part.class, entry.audience(), Channel.VALUES);
        this.parts = parts;
        this.track = entry.track();
        this.modCounter = entry.modCounter();

        audience().add(Channel.SetEvents, new SetName());
        audience().add(Channel.SetEvents, new SetMidiChannels());
    }

    static Factory factory(final Parts parts) {
        return track -> new Part(parts, parts.entryOf(track));
    }

    private static boolean isChannelEvent(final MidiEvent midiEvent) {
        return isChannelStatus(midiEvent.getMessage().getStatus());
    }

    @SuppressWarnings("MagicNumber")
    private static boolean isChannelStatus(final Integer status) {
        return (127 < status) && (status < 240);
    }

    @SuppressWarnings("MagicNumber")
    private static int channelOf(final MidiEvent midiEvent) {
        return midiEvent.getMessage().getStatus() & 0x0f;
    }

    private static void shift(final MidiEvent midiEvent, final long delta) {
        final long oldTime = midiEvent.getTick();
        if ((0L == oldTime) && Util.Message.Type.META.isTypeOf(midiEvent.getMessage())) {
            // keep it in place -> nothing to do!
        } else {
            final long newTime = Math.max(0L, oldTime + delta);
            midiEvent.setTick(newTime);
        }
    }

    private Stream<MidiEvent> stream() {
        return Util.stream(track);
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

    @SuppressWarnings("OverloadedVarargsMethod")
    public final Part add(final MidiEvent... events) {
        return add(Arrays.asList(events));
    }

    public final Part add(final Iterable<? extends MidiEvent> events) {
        synchronized (track) {
            for (final MidiEvent event : events) {
                track.add(event);
            }
        }
        return setModified();
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final Part remove(final MidiEvent... events) {
        return remove(Arrays.asList(events));
    }

    public final Part remove(final Iterable<? extends MidiEvent> events) {
        synchronized (track) {
            for (final MidiEvent event : events) {
                track.remove(event);
            }
        }
        return setModified();
    }

    public final MidiEvent get(final int index) {
        synchronized (track) {
            return track.get(index);
        }
    }

    public final int size() {
        synchronized (track) {
            return track.size();
        }
    }

    public final String getPrefix() {
        return String.format("Track %02d", parts.indexOf(track));
    }

    final Track backing() {
        return track;
    }

    public final boolean isModified() {
        return 0L != modCounter.get();
    }

    private Part setModified() {
        features.reset();
        modCounter.incrementAndGet();
        parts.onModifiedTrack();
        return fire(Channel.SetEvents, Channel.SetModified);
    }

    final void resetModified() {
        modCounter.set(0);
        fire(Channel.SetModified);
    }

    @SuppressWarnings("UnusedReturnValue")
    public final Part shift(final long delta) {
        synchronized (track) {
            stream().toList()
                    .forEach(midiEvent -> shift(midiEvent, delta));
        }
        return setModified();
    }

    final Map<Integer, List<MidiEvent>> extractChannels() {
        final Map<Integer, List<MidiEvent>> result;
        synchronized (track) {
            result = stream().filter(Part::isChannelEvent)
                             .collect(groupingBy(Part::channelOf));
            result.values().stream()
                  .flatMap(List::stream)
                  .forEach(track::remove);
        }
        setModified();
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof final Part other) && (track == other.track));
    }

    @Override
    public final int hashCode() {
        return track.hashCode();
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(\"%s\", %d)".formatted(name(), size());
    }

    @FunctionalInterface
    public interface Channel extends Sender.Channel<Part, Part> {

        Channel SetChannels = midiTrack -> midiTrack;
        Channel SetEvents = midiTrack -> midiTrack;
        Channel SetModified = midiTrack -> midiTrack;
        Channel SetName = midiTrack -> midiTrack;

        @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
        Set<Channel> VALUES = Set.of(SetChannels, SetEvents, SetModified, SetName);
    }

    @FunctionalInterface
    interface Factory {
        Part create(Track track);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends LazyFeatures.Key<Features, R> {

        Key<List<MidiEvent>> LIST = Features::newList;
        Key<SortedSet<Integer>> MIDI_CHANNELS = Features::newMidiChannels;
        Key<String> NAME = Features::newName;
    }

    private static final class SetMidiChannels implements Consumer<Part> {

        private Set<Integer> lastMidiChannels = null;

        @Override
        public void accept(final Part track) {
            final Set<Integer> newMidiChannels = track.features.get(Key.MIDI_CHANNELS);
            if (!newMidiChannels.equals(lastMidiChannels)) {
                lastMidiChannels = newMidiChannels;
                track.fire(Channel.SetChannels);
            }
        }
    }

    private static final class SetName implements Consumer<Part> {

        private String lastName = null;

        @Override
        public final void accept(final Part track) {
            final String newName = track.features.get(Key.NAME);
            if (!newName.equals(lastName)) {
                lastName = newName;
                track.fire(Channel.SetName);
            }
        }
    }

    private final class Features extends LazyFeatures<Features> {

        @Override
        protected final Features host() {
            return this;
        }

        private List<MidiEvent> newList() {
            synchronized (track) {
                return stream().toList();
            }
        }

        private SortedSet<Integer> newMidiChannels() {
            synchronized (track) {
                final SortedSet<Integer> result =
                        stream().map(MidiEvent::getMessage)
                                .map(MidiMessage::getStatus)
                                .filter(Part::isChannelStatus) // <-> isChannelMessage
                                .map(status -> status & 0x0f)
                                .collect(Collectors.toCollection(TreeSet::new));
                return Collections.unmodifiableSortedSet(result);
            }
        }

        private String newName() {
            synchronized (track) {
                return stream().map(MidiEvent::getMessage)
                               .filter(Util.MetaMessage.Type.TRACK_NAME::isValid)
                               .map(Util.MetaMessage::trackName)
                               .findFirst()
                               .orElse("[undefined]");
            }
        }
    }
}
