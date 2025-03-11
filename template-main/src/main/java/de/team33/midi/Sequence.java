package de.team33.midi;

import de.team33.miditor.IClickParameter;
import de.team33.patterns.notes.alpha.Channel;
import de.team33.midix.Timing;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequencer;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public interface Sequence {

    void associate(Sequencer sequencer) throws InvalidMidiDataException;

    MidiTrack create();

    MidiTrack create(IClickParameter cp);

    void delete(Iterable<MidiTrack> tracks);

    boolean delete(MidiTrack track);

    boolean equals(Object obj);

    File getFile();

    int getTempo();

    void setTempo(int tempo);

    long getTickLength();

    Timing getTiming();

    MidiTrack[] getTracks();

    boolean isModified();

    void join(Iterable<MidiTrack> tracks);

    void save() throws IOException;

    void save_as(File file) throws IOException;

    void split(MidiTrack track);

    void addListener(Event event, Consumer<? super Sequence> listener);

    enum Event implements Channel<Sequence> {
        SetFile,
        SetModified,
        SetParts
    }
}
