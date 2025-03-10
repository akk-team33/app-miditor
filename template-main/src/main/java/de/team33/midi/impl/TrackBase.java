package de.team33.midi.impl;

import de.team33.midi.Track;
import de.team33.patterns.notes.alpha.Audience;

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

public abstract class TrackBase implements Track {

    private static final String FMT_PREFIX =
            "Track %02d";
    private static final String NO_NAME =
            "- Kein Name -";
    private static final Set<Event> INITIAL_EVENTS =
            Set.of(Event.SetChannels, Event.SetEvents, Event.SetModified, Event.SetName);

    private final Audience audience = new Audience();
    private final javax.sound.midi.Track midiTrack;
    private int[] channels = new int[0];
    private String name = "";
    private boolean modified = false;

    public TrackBase(final javax.sound.midi.Track t) {
        midiTrack = t;
        addListener(Event.SetEvents, this::onSetEvents);
    }

    @Override
    public final void addListener(final Event event, final Consumer<? super Track> listener) {
        audience.add(event, listener);
        if (INITIAL_EVENTS.contains(event)) {
            listener.accept(this);
        }
    }

    public final void add(final MidiEvent... midiEvents) {
        synchronized (midiTrack) {
            final Set<Event> events = EnumSet.noneOf(Event.class);
            final int length = midiEvents.length;
            for (final MidiEvent midiEvent : midiEvents) {
                core_add(midiEvent, events);
            }

            relay(events);
        }
    }

    private boolean core_add(final MidiEvent midiEvent, final Set<Event> events) {
        if (midiTrack.add(midiEvent)) {
            core_clear(events);
            return true;
        } else {
            return false;
        }
    }

    private void core_clear(final Set<Event> events) {
        core_modify(true, events);
        events.add(Event.SetEvents);
    }

    private void core_modify(final boolean isModified, final Set<? super Event> events) {
        if (modified != isModified) {
            modified = isModified;
            events.add(Event.SetModified);
        }
    }

    private void core_remove(final MidiEvent midiEvent, final Set<Event> events) {
        if (midiTrack.remove(midiEvent)) {
            core_clear(events);
        }
    }

    private void core_shift(final MidiEvent oldMidiEvent, final long delta, final Set<Event> events) {
        final long oldTime = oldMidiEvent.getTick();
        long newTime = oldTime + delta;
        if (0L > newTime) {
            newTime = 0L;
        }

        if (newTime != oldTime) {
            final MidiEvent newEvent = new MidiEvent(oldMidiEvent.getMessage(), newTime);
            core_remove(oldMidiEvent, events);
            core_add(newEvent, events);
        }
    }

    public final Map<Integer, List<MidiEvent>> extractChannels() {
        synchronized (midiTrack) {
            final Set<Event> events = EnumSet.noneOf(Event.class);
            final Map<Integer, List<MidiEvent>> result = new TreeMap<>();

            for (final MidiEvent midiEvent : getAll()) {
                final MidiMessage midiMessage = midiEvent.getMessage();
                final int status = midiMessage.getStatus();
                if (128 <= status && 240 > status) {
                    final int channel = status & 15;
                    result.computeIfAbsent(channel, key -> new ArrayList<>(0))
                          .add(midiEvent);
                    core_remove(midiEvent, events);
                }
            }

            relay(events);
            return result;
        }
    }

    public final MidiEvent get(final int index) {
        synchronized (midiTrack) {
            return midiTrack.get(index);
        }
    }

    public final MidiEvent[] getAll() {
        synchronized (midiTrack) {
            final MidiEvent[] ret = new MidiEvent[size()];

            for (int i = 0; i < ret.length; ++i) {
                ret[i] = midiTrack.get(i);
            }

            return ret;
        }
    }

    public final int[] getChannels() {
        return channels.clone();
    }

    protected abstract int getIndex();

    public final String getName() {
        return name;
    }

    public final String getPrefix() {
        return String.format("Track %02d", getIndex());
    }

    @SuppressWarnings("DesignForExtension")
    protected javax.sound.midi.Track getMidiTrack() {
        return midiTrack;
    }

    public final boolean isModified() {
        return modified;
    }

    @SuppressWarnings("DesignForExtension")
    protected void setModified(final boolean isModified) {
        final Set<Event> events = EnumSet.noneOf(Event.class);
        core_modify(isModified, events);
        relay(events);
    }

    private void relay(final Set<Event> events) {
        events.forEach(event -> audience.send(event, this));
    }

    public final void remove(final MidiEvent... midiEvents) {
        synchronized (midiTrack) {
            final Set<Event> events = EnumSet.noneOf(Event.class);
            Arrays.stream(midiEvents)
                  .forEach(event -> core_remove(event, events));
            relay(events);
        }
    }

    public final void shift(final long delta) {
        synchronized (midiTrack) {
            final Set<Event> messages = EnumSet.noneOf(Event.class);
            Arrays.stream(getAll())
                  .forEach(event -> core_shift(event, delta, messages));
            relay(messages);
        }
    }

    public final int size() {
        synchronized (midiTrack) {
            return midiTrack.size();
        }
    }

    private void onSetEvents(final Track track) {
        synchronized (midiTrack) {
            final Set<Event> events = EnumSet.noneOf(Event.class);
            String newName = "- Kein Name -";
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
                } else if ("- Kein Name -" == newName && 255 == status) {
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
                events.add(Event.SetName);
            }

            if (!Arrays.equals(newChannels, channels)) {
                channels = newChannels;
                events.add(Event.SetChannels);
            }

            relay(events);
        }
    }
}
