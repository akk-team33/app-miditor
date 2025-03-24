package de.team33.midi;

import de.team33.midix.Midi;
import de.team33.patterns.lazy.narvi.LazyFeatures;
import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings({"ClassNamePrefixedWithPackageName", "ClassWithTooManyMethods"})
public final class MidiTrack extends Sender<MidiTrack> {

    private final TrackList trackList;
    private final TrackList.Entry entry;
    private final Mapping mapping;
    private final Features features = new Features();

    private MidiTrack(final TrackList trackList, final TrackList.Entry entry) {
        super(MidiTrack.class);
        this.trackList = trackList;
        this.entry = entry;
        this.mapping = Mapping.builder()
                              .put(Channel.SetEvents, () -> this)
                              .put(Channel.SetModified, () -> this)
                              .put(Channel.SetChannels, () -> this)
                              .put(Channel.SetName, () -> this)
                              .build();

        addPlain(Channel.SetEvents, new SetName());
        addPlain(Channel.SetEvents, new SetMidiChannels());
    }

    static Factory factory(final TrackList trackList) {
        return track -> new MidiTrack(trackList, trackList.entryOf(track));
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
        if ((0L == oldTime) && Midi.Message.Type.META.isTypeOf(midiEvent.getMessage())) {
            // keep it in place -> nothing to do!
        } else {
            final long newTime = Math.max(0L, oldTime + delta);
            midiEvent.setTick(newTime);
        }
    }

    @Override
    protected final Audience audience() {
        return entry.audience();
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
    }

    private Stream<MidiEvent> stream() {
        return Util.stream(entry.track());
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
    public final MidiTrack add(final MidiEvent... events) {
        return add(Arrays.asList(events));
    }

    public final MidiTrack add(final Iterable<? extends MidiEvent> events) {
        synchronized (entry.track()) {
            for (final MidiEvent event : events) {
                entry.track().add(event);
            }
        }
        return setModified();
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final MidiTrack remove(final MidiEvent... events) {
        return remove(Arrays.asList(events));
    }

    public final MidiTrack remove(final Iterable<? extends MidiEvent> events) {
        synchronized (entry.track()) {
            for (final MidiEvent event : events) {
                entry.track().remove(event);
            }
        }
        return setModified();
    }

    public final MidiEvent get(final int index) {
        synchronized (entry.track()) {
            return entry.track().get(index);
        }
    }

    public final int size() {
        synchronized (entry.track()) {
            return entry.track().size();
        }
    }

    public final String getPrefix() {
        return String.format("Track %02d", trackList.indexOf(entry.track()));
    }

    final Track backing() {
        return entry.track();
    }

    public final boolean isModified() {
        return 0L != entry.modCounter().get();
    }

    private MidiTrack setModified() {
        features.reset();
        entry.modCounter().incrementAndGet();
        return fire(Channel.SetEvents, Channel.SetModified);
    }

    @SuppressWarnings("UnusedReturnValue")
    public final MidiTrack shift(final long delta) {
        synchronized (entry.track()) {
            stream().toList()
                    .forEach(midiEvent -> shift(midiEvent, delta));
        }
        return setModified();
    }

    final Map<Integer, List<MidiEvent>> extractChannels() {
        final Map<Integer, List<MidiEvent>> result;
        synchronized (entry.track()) {
            result = stream().filter(MidiTrack::isChannelEvent)
                             .collect(groupingBy(MidiTrack::channelOf));
            result.values().stream()
                  .flatMap(List::stream)
                  .forEach(event -> entry.track().remove(event));
        }
        setModified();
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof final MidiTrack other) && (entry.track() == other.entry.track()));
    }

    @Override
    public final int hashCode() {
        return entry.track().hashCode();
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(\"%s\", %d)".formatted(name(), size());
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    public enum Channel implements de.team33.patterns.notes.alpha.Channel<MidiTrack> {
        // TODO?: Released,
        SetChannels,
        SetEvents,
        SetModified,
        SetName
    }

    @FunctionalInterface
    interface Factory {
        MidiTrack create(Track track);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends LazyFeatures.Key<Features, R> {

        Key<List<MidiEvent>> LIST = Features::newList;
        Key<SortedSet<Integer>> MIDI_CHANNELS = Features::newMidiChannels;
        Key<String> NAME = Features::newName;
    }

    private static final class SetMidiChannels implements Consumer<MidiTrack> {

        private Set<Integer> lastMidiChannels = null;

        @Override
        public void accept(final MidiTrack track) {
            final Set<Integer> newMidiChannels = track.features.get(Key.MIDI_CHANNELS);
            if (!newMidiChannels.equals(lastMidiChannels)) {
                lastMidiChannels = newMidiChannels;
                track.fire(Channel.SetChannels);
            }
        }
    }

    private static final class SetName implements Consumer<MidiTrack> {

        private String lastName = null;

        @Override
        public final void accept(final MidiTrack track) {
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
            synchronized (entry.track()) {
                return stream().toList();
            }
        }

        private SortedSet<Integer> newMidiChannels() {
            synchronized (entry.track()) {
                final SortedSet<Integer> result =
                        stream().map(MidiEvent::getMessage)
                                .map(MidiMessage::getStatus)
                                .filter(MidiTrack::isChannelStatus) // <-> isChannelMessage
                                .map(status -> status & 0x0f)
                                .collect(Collectors.toCollection(TreeSet::new));
                return Collections.unmodifiableSortedSet(result);
            }
        }

        private String newName() {
            synchronized (entry.track()) {
                return stream().map(MidiEvent::getMessage)
                               .filter(Midi.MetaMessage.Type.TRACK_NAME::isValid)
                               .map(Midi.MetaMessage::trackName)
                               .findFirst()
                               .orElse("[undefined]");
            }
        }
    }
}
