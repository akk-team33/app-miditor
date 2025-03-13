package de.team33.midi;

import de.team33.midix.Timing;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequencer;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

public interface Sequence {

    // TODO ...
    void associate(Sequencer sequencer) throws InvalidMidiDataException;

    default MidiTrack create(final MidiEvent... events){
        return create(Arrays.asList(events));
    }

    MidiTrack create(final Iterable<? extends MidiEvent> events);

    void delete(Iterable<MidiTrack> tracks);

    boolean delete(MidiTrack track);

    boolean equals(Object obj);

    // TODO ...
    File getFile();

    int getTempo();

    void setTempo(int tempo);

    long getTickLength();

    Timing getTiming();

    MidiTrack[] getTracks();

    boolean isModified();

    void join(Iterable<MidiTrack> tracks);

    // TODO ...
    void save() throws IOException;

    // TODO ...
    void save_as(File file) throws IOException;

    void split(MidiTrack track);

    // TODO ...
    void addListener(Channel channel, Consumer<? super Sequence> listener);

    @SuppressWarnings("ClassNameSameAsAncestorName")
    enum Channel implements de.team33.patterns.notes.alpha.Channel<Sequence> {
        SetFile,
        SetModified,
        SetParts
    }
}
