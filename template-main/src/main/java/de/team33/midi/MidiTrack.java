package de.team33.midi;

import de.team33.patterns.notes.alpha.Audience;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

public class MidiTrack {

    private static final String FMT_PREFIX = "Track %02d";
    private static final String NO_NAME = "- No Name -";
    private static final Set<Channel> INITIAL_CHANNELS =
            Set.of(Channel.SetChannels, Channel.SetEvents, Channel.SetModified, Channel.SetName);

    private final Audience audience = new Audience();
    private final javax.sound.midi.Track backing;
    private final int index;
    private int[] channels = new int[0];
    private String name = "";
    private boolean modified = false;

    public MidiTrack(final int index, final Track backing) {
        this.backing = backing;
        this.index = index;
        add(Channel.SetEvents, this::onSetEvents);
    }

    public final void add(final Channel channel, final Consumer<? super MidiTrack> listener) {
        audience.add(channel, listener);
        if (INITIAL_CHANNELS.contains(channel)) {
            listener.accept(this);
        }
    }

    public final void add(final MidiEvent... midiEvents) {
        synchronized (backing) {
            final Set<Channel> channels = EnumSet.noneOf(Channel.class);
            for (final MidiEvent midiEvent : midiEvents) {
                core_add(midiEvent, channels);
            }
            relay(channels);
        }
    }

    private boolean core_add(final MidiEvent midiEvent, final Set<Channel> channels) {
        if (backing.add(midiEvent)) {
            core_clear(channels);
            return true;
        } else {
            return false;
        }
    }

    private void core_clear(final Set<Channel> channels) {
        core_modify(true, channels);
        channels.add(Channel.SetEvents);
    }

    private void core_modify(final boolean isModified, final Set<? super Channel> events) {
        if (modified != isModified) {
            modified = isModified;
            events.add(Channel.SetModified);
        }
    }

    private void core_remove(final MidiEvent midiEvent, final Set<Channel> channels) {
        if (backing.remove(midiEvent)) {
            core_clear(channels);
        }
    }

    private void core_shift(final MidiEvent oldMidiEvent, final long delta, final Set<Channel> channels) {
        final long oldTime = oldMidiEvent.getTick();
        long newTime = oldTime + delta;
        if (0L > newTime) {
            newTime = 0L;
        }

        if (newTime != oldTime) {
            final MidiEvent newEvent = new MidiEvent(oldMidiEvent.getMessage(), newTime);
            core_remove(oldMidiEvent, channels);
            core_add(newEvent, channels);
        }
    }

    public final Map<Integer, List<MidiEvent>> extractChannels() {
        synchronized (backing) {
            final Set<Channel> channels = EnumSet.noneOf(Channel.class);
            final Map<Integer, List<MidiEvent>> result = new TreeMap<>();

            for (final MidiEvent midiEvent : list()) {
                final MidiMessage midiMessage = midiEvent.getMessage();
                final int status = midiMessage.getStatus();
                if (128 <= status && 240 > status) {
                    final int channel = status & 15;
                    result.computeIfAbsent(channel, key -> new ArrayList<>(0))
                          .add(midiEvent);
                    core_remove(midiEvent, channels);
                }
            }

            relay(channels);
            return result;
        }
    }

    public final MidiEvent get(final int index) {
        synchronized (backing) {
            return backing.get(index);
        }
    }

    public final MidiEvent[] list() {
        synchronized (backing) {
            final MidiEvent[] ret = new MidiEvent[size()];

            for (int i = 0; i < ret.length; ++i) {
                ret[i] = backing.get(i);
            }

            return ret;
        }
    }

    public final int[] midiChannels() {
        return channels.clone();
    }

    // TODO: make package private!
    public final int index() {
        return index;
    }

    public final String name() {
        return name;
    }

    public final String getPrefix() {
        return String.format(FMT_PREFIX, index);
    }

    // TODO: make package private!
    public final javax.sound.midi.Track backing() {
        return backing;
    }

    public final boolean isModified() {
        return modified;
    }

    // TODO: make private!
    @SuppressWarnings("DesignForExtension")
    public final void setModified(final boolean isModified) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        core_modify(isModified, channels);
        relay(channels);
    }

    private void relay(final Set<Channel> channels) {
        channels.forEach(event -> audience.send(event, this));
    }

    public final void remove(final MidiEvent... midiEvents) {
        synchronized (backing) {
            final Set<Channel> channels = EnumSet.noneOf(Channel.class);
            Arrays.stream(midiEvents)
                  .forEach(event -> core_remove(event, channels));
            relay(channels);
        }
    }

    public final void shift(final long delta) {
        synchronized (backing) {
            final Set<Channel> messages = EnumSet.noneOf(Channel.class);
            Arrays.stream(list())
                  .forEach(event -> core_shift(event, delta, messages));
            relay(messages);
        }
    }

    public final int size() {
        synchronized (backing) {
            return backing.size();
        }
    }

    private void onSetEvents(final MidiTrack track) {
        synchronized (backing) {
            final Set<Channel> channels = EnumSet.noneOf(Channel.class);
            String newName = NO_NAME;
            int nChannels = 0;
            final int[] nPerChannel = new int[16];
            int i = 0;

            int ix;
            for (ix = backing.size(); i < ix; ++i) {
                final MidiEvent evnt = backing.get(i);
                final MidiMessage mssg = evnt.getMessage();
                final int status = mssg.getStatus();
                if (128 <= status && 240 > status) {
                    final int channel = status & 15;
                    ++nPerChannel[channel];
                    if (1 == nPerChannel[channel]) {
                        ++nChannels;
                    }
                } else if (NO_NAME.equals(newName) && 255 == status) {
                    final byte[] b = mssg.getMessage();
                    if (2 < b.length && 3 == b[1] && b[2] == b.length - 3) {
                        newName = new String(b, 3, b.length - 3, StandardCharsets.UTF_8);
                    }
                }
            }

            final int[] newChannels = new int[nChannels];
            ix = 0;

            for (int k = 0; ix < nPerChannel.length; ++ix) {
                if (0 < nPerChannel[ix]) {
                    newChannels[k] = ix;
                    k++;
                }
            }

            if (!newName.equals(name)) {
                name = newName;
                channels.add(Channel.SetName);
            }

            if (!Arrays.equals(newChannels, this.channels)) {
                this.channels = newChannels;
                channels.add(Channel.SetChannels);
            }

            relay(channels);
        }
    }

    public enum Channel implements de.team33.patterns.notes.alpha.Channel<MidiTrack> {
        // TODO?: Released,
        SetChannels,
        SetEvents,
        SetModified,
        SetName
    }
}
