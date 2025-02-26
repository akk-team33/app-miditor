package de.team33.midi;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequencer;
import net.team33.messaging.Registry;
import net.team33.miditor.IClickParameter;

public interface Sequence extends Registry<Sequence.Message> {
    void associate(Sequencer var1) throws InvalidMidiDataException;

    Track create();

    Track create(IClickParameter var1);

    void delete(Iterable<Track> var1);

    boolean delete(Track var1);

    boolean equals(Object var1);

    File getFile();

    int getTempo();

    long getTickLength();

    Timing getTiming();

    Track[] getTracks();

    boolean isModified();

    void join(Iterable<Track> var1);

    void save() throws IOException;

    void save_as(File var1) throws IOException;

    void setTempo(int var1);

    void split(Track var1);

    public interface Message extends net.team33.messaging.Message<Sequence> {
    }

    public interface SetFile extends Message {
    }

    public interface SetModified extends Message {
    }

    public interface SetParts extends Message {
    }
}
