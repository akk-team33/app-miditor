package de.team33.midi;

import de.team33.miditor.IClickParameter;
import de.team33.patterns.notes.eris.Channel;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequencer;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public interface Sequence {

    void associate(Sequencer sequencer) throws InvalidMidiDataException;

    Track create();

    Track create(IClickParameter cp);

    void delete(Iterable<Track> tracks);

    boolean delete(Track track);

    boolean equals(Object obj);

    File getFile();

    int getTempo();

    void setTempo(int tempo);

    long getTickLength();

    Timing getTiming();

    Track[] getTracks();

    boolean isModified();

    void join(Iterable<Track> tracks);

    void save() throws IOException;

    void save_as(File file) throws IOException;

    void split(Track track);

    void addListener(Event event, Consumer<? super Sequence> listener);

    enum Event implements Channel<Sequence> {
        SetFile,
        SetModified,
        SetParts
    }
}
