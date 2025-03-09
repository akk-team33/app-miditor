package de.team33.midi;

import de.team33.messaging.Message;
import de.team33.messaging.Registry;

import javax.sound.midi.MidiEvent;
import java.util.List;
import java.util.Map;

public interface MidiTrack extends Registry<Message<MidiTrack>> {
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

    public interface Released extends Message<MidiTrack> {
    }

    public interface SetChannels extends Message<MidiTrack> {
    }

    public interface SetEvents extends Message<MidiTrack> {
    }

    public interface SetModified extends Message<MidiTrack> {
    }

    public interface SetName extends Message<MidiTrack> {
    }
}
