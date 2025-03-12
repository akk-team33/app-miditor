package de.team33.midi;

import de.team33.midix.Midi;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;
import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class TrackProxy extends Sender<TrackProxy> {

    private final Audience audience;
    private final Mapping mapping;
    private final int index;
    private final Track backing;
    private final AtomicBoolean modification = new AtomicBoolean(false);
    private final Features features = new Features();

    public TrackProxy(final int index, final Track backing) {
        super(TrackProxy.class);
        this.audience = new Audience(new SimpleAsyncExecutor());
        this.mapping = Mapping.builder()
                              .put(Internal.SetModified, () -> this)
                              .put(Channel.SetEvents, () -> this)
                              .put(Channel.SetModified, () -> this)
                              .put(Channel.SetChannels, () -> this)
                              .put(Channel.SetName, () -> this)
                              .build();
        this.index = index;
        this.backing = backing;
        addPlain(Internal.SetModified, new SetModified());
        addPlain(Internal.SetModified, new SetName());
        addPlain(Internal.SetModified, new SetMidiChannels());
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

    @Override
    protected final Audience audience() {
        return audience;
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
    }

    private TrackProxy setModified(final boolean modified) {
        modification.set(modified);
        features.reset();
        return fire(Internal.SetModified, Channel.SetEvents);
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
        }
        return setModified(true);
    }

    public final TrackProxy remove(final MidiEvent... events) {
        return remove(Arrays.asList(events));
    }

    public final TrackProxy remove(final Collection<? extends MidiEvent> events) {
        synchronized (backing) {
            events.forEach(backing::remove);
        }
        return setModified(true);
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

    @Deprecated // may stay as package private (?)
    public final Track backing() {
        return backing;
    }

    public final boolean isModified() {
        return modification.get();
    }

    final TrackProxy resetModified() {
        return setModified(false);
    }

    public final TrackProxy shift(final long delta) {
        synchronized (backing) {
            stream().toList()
                    .forEach(midiEvent -> shift(midiEvent, delta));
        }
        return setModified(true);
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
            result = stream().filter(TrackProxy::isChannelEvent)
                             .collect(groupingBy(TrackProxy::channelOf));
            result.values().stream()
                  .flatMap(List::stream)
                  .forEach(backing::remove);
        }
        setModified(true);
        return result;
    }

    public enum Channel implements de.team33.patterns.notes.alpha.Channel<TrackProxy> {
        // TODO?: Released,
        SetChannels,
        SetEvents,
        SetModified,
        SetName
    }

    private interface Internal extends de.team33.patterns.notes.alpha.Channel<TrackProxy> {

        Internal SetModified = () -> "Internal:SetModified";
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<TrackProxy, R> {

        Key<List<MidiEvent>> LIST = host -> host.features.newList();
        Key<SortedSet<Integer>> MIDI_CHANNELS = host -> host.features.newMidiChannels();
        Key<String> NAME = host -> host.features.newName();
    }

    private static final class SetMidiChannels implements Consumer<TrackProxy> {

        private Set<Integer> lastMidiChannels = null;

        @Override
        public void accept(final TrackProxy track) {
            final Set<Integer> newMidiChannels = track.features.get(Key.MIDI_CHANNELS);
            if (!newMidiChannels.equals(lastMidiChannels)) {
                lastMidiChannels = newMidiChannels;
                track.fire(Channel.SetChannels);
            }
        }
    }

    private static final class SetName implements Consumer<TrackProxy> {

        private String lastName = null;

        @Override
        public final void accept(final TrackProxy track) {
            final String newName = track.features.get(Key.NAME);
            if (!newName.equals(lastName)) {
                lastName = newName;
                track.fire(Channel.SetName);
            }
        }
    }

    private static final class SetModified implements Consumer<TrackProxy> {

        private boolean lastModified = false;

        @Override
        public final void accept(final TrackProxy track) {
            final boolean newModified = track.modification.get();
            if (newModified != lastModified) {
                lastModified = newModified;
                track.fire(Channel.SetModified);
            }
        }
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
