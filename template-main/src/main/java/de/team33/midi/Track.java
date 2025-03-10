package de.team33.midi;

import de.team33.patterns.notes.alpha.Channel;

import javax.sound.midi.MidiEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface Track {
    void add(MidiEvent... var1);

    Map<Integer, List<MidiEvent>> extractChannels();

    MidiEvent get(int index);

    MidiEvent[] getAll();

    int[] getChannels();

    String getName();

    String getPrefix();

    boolean isModified();

    void remove(MidiEvent... midiEvents);

    void shift(long delta);

    int size();

    void addListener(Event event, Consumer<? super Track> listener);

    enum Event implements Channel<Track> {
        // TODO?: Released,
        SetChannels,
        SetEvents,
        SetModified,
        SetName
    }
}
