package de.team33.midi.impl;

import de.team33.midi.Track;
import de.team33.midi.proxy.SequenceProxy;
import de.team33.midi.proxy.TrackProxy;
import de.team33.patterns.notes.eris.Audience;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TrackBase implements Track {

    private static final String FMT_PREFIX =
            "Track %02d";
    private static final String NO_NAME =
            "- No Name -";
    private static final Set<Route> INITIAL_ROUTES =
            Set.of(Route.SetEvents, Route.SetModified);
    private static final int CMS_MIN = 128;
    private static final int CMS_BOUND = 240;
    private static final int CHANNEL_BITS = 15;

    private final Audience audience = new Audience();
    private final SequenceProxy midiSequence;
    private final TrackProxy midiTrack;
    private boolean modified = false;

    TrackBase(final SequenceProxy midiSequence, final TrackProxy midiTrack) {
        this.midiSequence = midiSequence;
        this.midiTrack = midiTrack;
    }

    @Override
    public final void addListener(final Route route, final Consumer<? super Track> listener) {
        audience.add(route, listener);
        if (INITIAL_ROUTES.contains(route)) {
            listener.accept(this);
        }
    }

    @Override
    public final void add(final MidiEvent... midiEvents) {
        synchronized (midiTrack) {
            final Set<Route> routes = EnumSet.noneOf(Route.class);
            for (final MidiEvent midiEvent : midiEvents) {
                core_add(midiEvent, routes);
            }
            relay(routes);
        }
    }

    private void core_add(final MidiEvent midiEvent, final Set<? super Route> routes) {
        if (midiTrack.add(midiEvent)) {
            core_clear(routes);
        }
    }

    private void core_clear(final Set<? super Route> routes) {
        core_modify(true, routes);
        routes.add(Route.SetEvents);
    }

    private void core_modify(final boolean isModified, final Set<? super Route> events) {
        if (modified != isModified) {
            modified = isModified;
            events.add(Route.SetModified);
        }
    }

    private void core_remove(final MidiEvent midiEvent, final Set<? super Route> routes) {
        if (midiTrack.remove(midiEvent)) {
            core_clear(routes);
        }
    }

    private void core_shift(final MidiEvent oldMidiEvent, final long delta, final Set<? super Route> routes) {
        final long oldTime = oldMidiEvent.getTick();
        long newTime = oldTime + delta;
        if (0L > newTime) {
            newTime = 0L;
        }

        if (newTime != oldTime) {
            final MidiEvent newEvent = new MidiEvent(oldMidiEvent.getMessage(), newTime);
            core_remove(oldMidiEvent, routes);
            core_add(newEvent, routes);
        }
    }

    @Override
    public final Map<Integer, List<MidiEvent>> extractChannels() {
        synchronized (midiTrack) {
            // TODO: stream().collect(Collectors.groupingBy(event -> event))
            final Set<Route> routes = EnumSet.noneOf(Route.class);
            final Map<Integer, List<MidiEvent>> result = new TreeMap<>();

            for (final MidiEvent midiEvent : getAll()) {
                final MidiMessage midiMessage = midiEvent.getMessage();
                final int status = midiMessage.getStatus();
                if (CMS_MIN <= status && CMS_BOUND > status) {
                    final int channel = status & CHANNEL_BITS;
                    result.computeIfAbsent(channel, key -> new ArrayList<>(0))
                          .add(midiEvent);
                    core_remove(midiEvent, routes);
                }
            }

            relay(routes);
            return result;
        }
    }

    @Override
    public final MidiEvent get(final int index) {
        synchronized (midiTrack) {
            return midiTrack.get(index);
        }
    }

    @Deprecated
    @Override
    public final MidiEvent[] getAll() {
        synchronized (midiTrack) {
            return stream().toArray(MidiEvent[]::new);
        }
    }

    @Override
    public final List<MidiEvent> toList() {
        synchronized (midiTrack) {
            return stream().toList();
        }
    }

    private Stream<MidiEvent> stream() {
        return IntStream.range(0, midiTrack.size())
                        .mapToObj(midiTrack::get);
    }

    @Override
    public final int[] getChannels() {
        synchronized (midiTrack) {
            return stream().map(MidiEvent::getMessage)
                           .mapToInt(MidiMessage::getStatus)
                           .filter(status -> CMS_MIN <= status)
                           .filter(status -> CMS_BOUND > status)
                           .map(status -> status & CHANNEL_BITS)
                           .distinct()
                           //TODO?: .sorted()
                           .toArray();
        }
    }

    private int getIndex() {
        return midiSequence.getTracks().indexOf(midiTrack);
    }

    @Override
    public final String getName() {
        synchronized (midiTrack) {
            return stream().map(MidiEvent::getMessage)
                           .filter(midiMessage -> 255 == midiMessage.getStatus())
                           .map(MidiMessage::getMessage)
                           .filter(bytes -> 2 < bytes.length)
                           .filter(bytes -> 3 == bytes[1])
                           .filter(bytes -> bytes.length - 3 == bytes[2])
                           .map(bytes -> new String(bytes, 3, bytes.length - 3, StandardCharsets.US_ASCII))
                           .findAny().orElse(NO_NAME);
        }
    }

    @Override
    public final String getPrefix() {
        return String.format(FMT_PREFIX, getIndex());
    }

    final TrackProxy getMidiTrack() {
        return midiTrack;
    }

    @Override
    public final boolean isModified() {
        return modified;
    }

    final void setModified(final boolean isModified) {
        final Set<Route> routes = EnumSet.noneOf(Route.class);
        core_modify(isModified, routes);
        relay(routes);
    }

    private void relay(final Set<Route> routes) {
        routes.forEach(event -> audience.send(event, this));
    }

    @Override
    public final void remove(final MidiEvent... midiEvents) {
        synchronized (midiTrack) {
            final Set<Route> routes = EnumSet.noneOf(Route.class);
            Arrays.stream(midiEvents)
                  .forEach(event -> core_remove(event, routes));
            relay(routes);
        }
    }

    @Override
    public final void shift(final long delta) {
        synchronized (midiTrack) {
            final Set<Route> messages = EnumSet.noneOf(Route.class);
            Arrays.stream(getAll())
                  .forEach(event -> core_shift(event, delta, messages));
            relay(messages);
        }
    }

    @Override
    public final int size() {
        synchronized (midiTrack) {
            return midiTrack.size();
        }
    }
}
