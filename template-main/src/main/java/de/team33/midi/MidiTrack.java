package de.team33.midi;

import de.team33.midix.Midi;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings({"ClassNamePrefixedWithPackageName", "ClassWithTooManyMethods"})
public final class MidiTrack extends Sender<MidiTrack> {

    private final Audience audience;
    private final Mapping mapping;
    private final int index;
    private final Track backing;
    private final ModificationCounter modificationCounter;
    private final Features features = new Features();

    private final Consumer<Integer> onModified;
    private final Consumer<Void> onReset;
    private final Consumer<Set<Integer>> onRemoved;

    private MidiTrack(final int index,
              final Track backing,
              final ModificationCounter modificationCounter,
              final Executor executor) {
        super(MidiTrack.class);
        this.audience = new Audience(executor);
        this.mapping = Mapping.builder()
                              .put(Channel.SetEvents, () -> this)
                              .put(Channel.SetModified, () -> this)
                              .put(Channel.SetChannels, () -> this)
                              .put(Channel.SetName, () -> this)
                              .build();
        this.index = index;
        this.backing = backing;
        this.modificationCounter = modificationCounter;

        addPlain(Channel.SetEvents, new SetName());
        addPlain(Channel.SetEvents, new SetMidiChannels());

        this.onReset = ignored -> fire(Channel.SetModified);
        this.onModified = this::onModified;
        this.onRemoved = this::onRemoved;

        modificationCounter.add(ModificationCounter.Channel.REMOVED, onRemoved);
        modificationCounter.add(ModificationCounter.Channel.SUB_MODIFIED, onModified);
        modificationCounter.add(ModificationCounter.Channel.RESET, onReset);
    }

    static Factory factory(final ModificationCounter modificationCounter, final Executor executor) {
        return (index1, track) -> new MidiTrack(index1, track, modificationCounter, executor);
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

    private void onRemoved(final Set<Integer> ids) {
        if (ids.contains(id())) {
            modificationCounter.remove(ModificationCounter.Channel.SUB_MODIFIED, onModified);
            modificationCounter.remove(ModificationCounter.Channel.RESET, onReset);
            modificationCounter.remove(ModificationCounter.Channel.REMOVED, onRemoved);
        }
    }

    private void onModified(final int id) {
        if (id == id()) {
            fire(Channel.SetModified);
        }
    }

    @Override
    protected final Audience audience() {
        return audience;
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
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

    @SuppressWarnings("OverloadedVarargsMethod")
    public final MidiTrack add(final MidiEvent... events) {
        return add(Arrays.asList(events));
    }

    public final MidiTrack add(final Iterable<? extends MidiEvent> events) {
        synchronized (backing) {
            for (final MidiEvent event : events) {
                backing.add(event);
            }
        }
        return setModified();
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final MidiTrack remove(final MidiEvent... events) {
        return remove(Arrays.asList(events));
    }

    public final MidiTrack remove(final Iterable<? extends MidiEvent> events) {
        synchronized (backing) {
            for (final MidiEvent event : events) {
                backing.remove(event);
            }
        }
        return setModified();
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

    final Track backing() {
        return backing;
    }

    public final boolean isModified() {
        return 0L != modificationCounter.get(id());
    }

    private MidiTrack setModified() {
        features.reset();
        modificationCounter.increment(id());
        return fire(Channel.SetEvents);
    }

    public final MidiTrack shift(final long delta) {
        synchronized (backing) {
            stream().toList()
                    .forEach(midiEvent -> shift(midiEvent, delta));
        }
        return setModified();
    }

    private void shift(final MidiEvent midiEvent, final long delta) {
        final long oldTime = midiEvent.getTick();
        if ((0L == oldTime) && Midi.Message.Type.META.isTypeOf(midiEvent.getMessage())) {
            // keep it in place -> nothing to do!
        } else {
            final long newTime = Math.max(0L, oldTime + delta);
            midiEvent.setTick(newTime);
        }
    }

    public final Map<Integer, List<MidiEvent>> extractChannels() {
        final Map<Integer, List<MidiEvent>> result;
        synchronized (backing) {
            result = stream().filter(MidiTrack::isChannelEvent)
                             .collect(groupingBy(MidiTrack::channelOf));
            result.values().stream()
                  .flatMap(List::stream)
                  .forEach(backing::remove);
        }
        setModified();
        return result;
    }

    final int id() {
        return System.identityHashCode(backing);
    }

    public enum Channel implements de.team33.patterns.notes.alpha.Channel<MidiTrack> {
        // TODO?: Released,
        SetChannels,
        SetEvents,
        SetModified,
        SetName
    }

    @FunctionalInterface
    interface Factory {
        MidiTrack create(int index, Track track);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<MidiTrack, R> {

        Key<List<MidiEvent>> LIST = host -> host.features.newList();
        Key<SortedSet<Integer>> MIDI_CHANNELS = host -> host.features.newMidiChannels();
        Key<String> NAME = host -> host.features.newName();
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

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends de.team33.patterns.features.alpha.Features<MidiTrack> {

        private Features() {
            super(ConcurrentHashMap::new);
        }

        @Override
        protected final MidiTrack host() {
            return MidiTrack.this;
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
