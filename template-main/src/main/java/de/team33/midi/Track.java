package de.team33.midi;

import net.team33.messaging.Message;
import net.team33.messaging.Registry;

import javax.sound.midi.MidiEvent;
import java.util.List;
import java.util.Map;

public interface Track extends Registry<Message<Track>> {
    void add(MidiEvent... var1);

    Map<Integer, List<MidiEvent>> extractChannels();

    MidiEvent get(int var1);

    MidiEvent[] getAll();

    int[] getChannels();

    String getName();

    String getPrefix();

    boolean isModified();

    void remove(MidiEvent... var1);

    void shift(long var1);

    int size();

    public interface Released extends Message<Track> {
    }

    public interface SetChannels extends Message<Track> {
    }

    public interface SetEvents extends Message<Track> {
    }

    public interface SetModified extends Message<Track> {
    }

    public interface SetName extends Message<Track> {
    }
}
