package de.team33.midi.impl;

import de.team33.midi.Track;
import net.team33.messaging.Listener;
import net.team33.messaging.Message;
import net.team33.messaging.Register;
import net.team33.messaging.sync.Router;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public abstract class TrackBase implements Track {
    private static final String FMT_PREFIX = "Track %02d";
    private static final String NO_NAME = "- Kein Name -";
    private final Track.SetChannels msgSetChannels = new SET_CHANNELS();
    private final Track.SetModified msgSetModified = new SET_MODIFIED();
    private final Track.SetName msgSetName = new SET_NAME();
    private final Track.SetEvents msgSetEvents = new SET_EVENTS();
    private final Track.Released msgReleased = new RELEASED();
    private final Router<Message<Track>> router = new Router();
    private final javax.sound.midi.Track midiTrack;
    private int[] channels = new int[0];
    private String name = "";
    private boolean modified = false;

    public TrackBase(javax.sound.midi.Track t) {
        this.router.addInitials(Arrays.asList(this.msgSetChannels, this.msgSetEvents, this.msgSetModified, this.msgSetName));
        this.midiTrack = t;
        this.router.getRegister(Track.SetEvents.class).add(new SET_EVENTS_LSTNR());
    }

    public final void add(MidiEvent... events) {
        synchronized(this.midiTrack) {
            Set<Message<Track>> messages = new HashSet();
            MidiEvent[] var7 = events;
            int var6 = events.length;

            for(int var5 = 0; var5 < var6; ++var5) {
                MidiEvent evnt = var7[var5];
                this.core_add(evnt, messages);
            }

            this.relay(messages);
        }
    }

    protected void clrRegister() {
        this.router.pass(this.msgReleased);
        this.router.clear();
        this.router.getRegister(Track.SetEvents.class).add(new SET_EVENTS_LSTNR());
    }

    private final boolean core_add(MidiEvent event, Set<Message<Track>> messages) {
        if (this.midiTrack.add(event)) {
            this.core_clear(messages);
            return true;
        } else {
            return false;
        }
    }

    private final void core_clear(Set<Message<Track>> messages) {
        this.core_modify(true, messages);
        messages.add(this.msgSetEvents);
    }

    private final void core_modify(boolean isModified, Set<Message<Track>> messages) {
        if (this.modified != isModified) {
            this.modified = isModified;
            messages.add(this.msgSetModified);
        }

    }

    private final boolean core_remove(MidiEvent event, Set<Message<Track>> messages) {
        if (this.midiTrack.remove(event)) {
            this.core_clear(messages);
            return true;
        } else {
            return false;
        }
    }

    private final void core_shift(MidiEvent oldEvent, long delta, Set<Message<Track>> messages) {
        long oldTime = oldEvent.getTick();
        long newTime = oldTime + delta;
        if (newTime < 0L) {
            newTime = 0L;
        }

        if (newTime != oldTime) {
            MidiEvent newEvent = new MidiEvent(oldEvent.getMessage(), newTime);
            this.core_remove(oldEvent, messages);
            this.core_add(newEvent, messages);
        }

    }

    public final Map<Integer, List<MidiEvent>> extractChannels() {
        synchronized(this.midiTrack) {
            Set<Message<Track>> messages = new HashSet();
            Map<Integer, List<MidiEvent>> ret = new TreeMap();
            MidiEvent[] allEvents = this.getAll();
            MidiEvent[] var8 = allEvents;
            int var7 = allEvents.length;

            for(int var6 = 0; var6 < var7; ++var6) {
                MidiEvent evnt = var8[var6];
                MidiMessage mssg = evnt.getMessage();
                int status = mssg.getStatus();
                if (128 <= status && status < 240) {
                    int channel = status & 15;
                    if (!ret.containsKey(channel)) {
                        ret.put(channel, new Vector());
                    }

                    ((List)ret.get(channel)).add(evnt);
                    this.core_remove(evnt, messages);
                }
            }

            this.relay(messages);
            return ret;
        }
    }

    public final MidiEvent get(int index) {
        synchronized(this.midiTrack) {
            return this.midiTrack.get(index);
        }
    }

    public final MidiEvent[] getAll() {
        synchronized(this.midiTrack) {
            MidiEvent[] ret = new MidiEvent[this.size()];

            for(int i = 0; i < ret.length; ++i) {
                ret[i] = this.midiTrack.get(i);
            }

            return ret;
        }
    }

    public final int[] getChannels() {
        return this.channels;
    }

    protected abstract int getIndex();

    public final String getName() {
        return this.name;
    }

    public final String getPrefix() {
        return String.format("Track %02d", this.getIndex());
    }

    public final <MSX extends Message<Track>> Register<MSX> getRegister(Class<MSX> msgClass) {
        return this.router.getRegister(msgClass);
    }

    protected javax.sound.midi.Track getTrack() {
        return this.midiTrack;
    }

    public final boolean isModified() {
        return this.modified;
    }

    private final void relay(Set<Message<Track>> messages) {
        Iterator var3 = messages.iterator();

        while(var3.hasNext()) {
            Message<Track> msg = (Message)var3.next();
            this.router.pass(msg);
        }

    }

    public final void remove(MidiEvent... events) {
        synchronized(this.midiTrack) {
            Set<Message<Track>> messages = new HashSet();
            MidiEvent[] var7 = events;
            int var6 = events.length;

            for(int var5 = 0; var5 < var6; ++var5) {
                MidiEvent event = var7[var5];
                this.core_remove(event, messages);
            }

            this.relay(messages);
        }
    }

    protected void setModified(boolean isModified) {
        Set<Message<Track>> messages = new HashSet();
        this.core_modify(isModified, messages);
        this.relay(messages);
    }

    public final void shift(long delta) {
        synchronized(this.midiTrack) {
            Set<Message<Track>> messages = new HashSet();
            MidiEvent[] events = this.getAll();
            MidiEvent[] var9 = events;
            int var8 = events.length;

            for(int var7 = 0; var7 < var8; ++var7) {
                MidiEvent event = var9[var7];
                this.core_shift(event, delta, messages);
            }

            this.relay(messages);
        }
    }

    public final int size() {
        synchronized(this.midiTrack) {
            return this.midiTrack.size();
        }
    }

    private class MESSAGE implements Message<Track> {
        private MESSAGE() {
        }

        public final Track getSender() {
            return TrackBase.this;
        }
    }

    private class RELEASED extends MESSAGE implements Track.Released {
        private RELEASED() {
            super();
        }
    }

    private class SET_CHANNELS extends MESSAGE implements Track.SetChannels {
        private SET_CHANNELS() {
            super();
        }
    }

    private class SET_EVENTS extends MESSAGE implements Track.SetEvents {
        private SET_EVENTS() {
            super();
        }
    }

    private class SET_EVENTS_LSTNR implements Listener<Track.SetEvents> {
        private SET_EVENTS_LSTNR() {
        }

        public void pass(Track.SetEvents message) {
            synchronized(TrackBase.this.midiTrack) {
                Set<Message<Track>> messages = new HashSet();
                String newName = "- Kein Name -";
                int nChannels = 0;
                int[] nPerChannel = new int[16];
                int i = 0;

                int ix;
                for(ix = TrackBase.this.midiTrack.size(); i < ix; ++i) {
                    MidiEvent evnt = TrackBase.this.midiTrack.get(i);
                    MidiMessage mssg = evnt.getMessage();
                    int status = mssg.getStatus();
                    if (128 <= status && status < 240) {
                        int channel = status & 15;
                        if (++nPerChannel[channel] == 1) {
                            ++nChannels;
                        }
                    } else if (newName == "- Kein Name -" && status == 255) {
                        byte[] b = mssg.getMessage();
                        if (b.length > 2 && b[1] == 3 && b[2] == b.length - 3) {
                            newName = new String(b, 3, b.length - 3);
                        }
                    }
                }

                int[] newChannels = new int[nChannels];
                ix = 0;

                for(int k = 0; ix < nPerChannel.length; ++ix) {
                    if (nPerChannel[ix] > 0) {
                        newChannels[k++] = ix;
                    }
                }

                if (!newName.equals(TrackBase.this.name)) {
                    TrackBase.this.name = newName;
                    messages.add(TrackBase.this.msgSetName);
                }

                if (!Arrays.equals(newChannels, TrackBase.this.channels)) {
                    TrackBase.this.channels = newChannels;
                    messages.add(TrackBase.this.msgSetChannels);
                }

                TrackBase.this.relay(messages);
            }
        }
    }

    private class SET_MODIFIED extends MESSAGE implements Track.SetModified {
        private SET_MODIFIED() {
            super();
        }
    }

    private class SET_NAME extends MESSAGE implements Track.SetName {
        private SET_NAME() {
            super();
        }
    }
}
