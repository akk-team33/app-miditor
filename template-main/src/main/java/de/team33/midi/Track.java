package de.team33.midi;

import de.team33.patterns.notes.eris.Channel;

import javax.sound.midi.MidiEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface Track {

    void add(MidiEvent... midiEvents);

    Map<Integer, List<MidiEvent>> extractChannels();

    MidiEvent get(int index);

    @Deprecated
    MidiEvent[] getAll();

    List<MidiEvent> toList();

    int[] getChannels();

    String getName();

    String getPrefix();

    boolean isModified();

    void remove(MidiEvent... midiEvents);

    void shift(long delta);

    int size();

    void addListener(Route route, Consumer<? super Track> listener);

    enum Route implements Channel<Track> {
        SetEvents,
        SetModified
    }
}
