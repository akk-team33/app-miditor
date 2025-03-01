package de.team33.midi.impl;

import de.team33.midi.Track;
import de.team33.midi.proxy.SequenceProxy;
import de.team33.midi.proxy.TrackProxy;
import de.team33.patterns.notes.eris.Audience;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class TrackBase implements Track {

    private static final String FMT_PREFIX =
            "Track %02d";
    private static final String NO_NAME =
            "- No Name -";
    private static final Set<Route> INITIAL_ROUTES =
            Set.of(Route.SetChannels, Route.SetEvents, Route.SetModified, Route.SetName);

    private final Audience audience = new Audience();
    private final SequenceProxy midiSequence;
    private final TrackProxy midiTrack;
    private int[] channels = new int[0];
    private String name = "";
    private boolean modified = false;

    public TrackBase(final SequenceProxy s, final TrackProxy t) {
        midiSequence = s;
        midiTrack = t;
        addListener(Route.SetEvents, this::onSetEvents);
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
            final int length = midiEvents.length;
            for (final MidiEvent midiEvent : midiEvents) {
                core_add(midiEvent, routes);
            }

            relay(routes);
        }
    }

    private boolean core_add(final MidiEvent midiEvent, final Set<Route> routes) {
        if (midiTrack.add(midiEvent)) {
            core_clear(routes);
            return true;
        } else {
            return false;
        }
    }

    private void core_clear(final Set<Route> routes) {
        core_modify(true, routes);
        routes.add(Route.SetEvents);
    }

    private void core_modify(final boolean isModified, final Set<? super Route> events) {
        if (modified != isModified) {
            modified = isModified;
            events.add(Route.SetModified);
        }
    }

    private void core_remove(final MidiEvent midiEvent, final Set<Route> routes) {
        if (midiTrack.remove(midiEvent)) {
            core_clear(routes);
        }
    }

    private void core_shift(final MidiEvent oldMidiEvent, final long delta, final Set<Route> routes) {
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
            final Set<Route> routes = EnumSet.noneOf(Route.class);
            final Map<Integer, List<MidiEvent>> result = new TreeMap<>();

            for (final MidiEvent midiEvent : getAll()) {
                final MidiMessage midiMessage = midiEvent.getMessage();
                final int status = midiMessage.getStatus();
                if (128 <= status && 240 > status) {
                    final int channel = status & 15;
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

    @Override
    public final MidiEvent[] getAll() {
        synchronized (midiTrack) {
            final MidiEvent[] ret = new MidiEvent[size()];

            for (int i = 0; i < ret.length; ++i) {
                ret[i] = midiTrack.get(i);
            }

            return ret;
        }
    }

    @Override
    public final int[] getChannels() {
        return channels.clone();
    }

    final int getIndex() {
        return midiSequence.getTracks().indexOf(midiTrack);
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getPrefix() {
        return String.format(FMT_PREFIX, getIndex());
    }

    @SuppressWarnings("DesignForExtension")
    protected TrackProxy getMidiTrack() {
        return midiTrack;
    }

    @Override
    public final boolean isModified() {
        return modified;
    }

    @SuppressWarnings("DesignForExtension")
    protected void setModified(final boolean isModified) {
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

    private void onSetEvents(final Track track) {
        synchronized (midiTrack) {
            final Set<Route> routes = EnumSet.noneOf(Route.class);
            String newName = NO_NAME;
            int nChannels = 0;
            final int[] nPerChannel = new int[16];
            int i = 0;

            int ix;
            for (ix = midiTrack.size(); i < ix; ++i) {
                final MidiEvent evnt = midiTrack.get(i);
                final MidiMessage mssg = evnt.getMessage();
                final int status = mssg.getStatus();
                if (128 <= status && 240 > status) {
                    final int channel = status & 15;
                    if (1 == ++nPerChannel[channel]) {
                        ++nChannels;
                    }
                } else if (NO_NAME == newName && 255 == status) {
                    final byte[] b = mssg.getMessage();
                    if (2 < b.length && 3 == b[1] && b[2] == b.length - 3) {
                        newName = new String(b, 3, b.length - 3);
                    }
                }
            }

            final int[] newChannels = new int[nChannels];
            ix = 0;

            for (int k = 0; ix < nPerChannel.length; ++ix) {
                if (0 < nPerChannel[ix]) {
                    newChannels[k++] = ix;
                }
            }

            if (!newName.equals(name)) {
                name = newName;
                routes.add(Route.SetName);
            }

            if (!Arrays.equals(newChannels, channels)) {
                channels = newChannels;
                routes.add(Route.SetChannels);
            }

            relay(routes);
        }
    }
}
