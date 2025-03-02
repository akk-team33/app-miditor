package de.team33.miditor.backend;

import de.team33.patterns.exceptional.dione.Converter;
import de.team33.patterns.exceptional.dione.Wrapping;
import de.team33.patterns.notes.eris.Audience;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.io.IOException;
import java.nio.file.Path;

public class MidiCenter {

    static final Converter CNV = Converter.using(Wrapping.method(IllegalStateException::new));

    private final Audience audience = new Audience();
    private final Sequencer sequencer;

    public MidiCenter() {
        this.sequencer = CNV.get(() -> MidiSystem.getSequencer(true));
    }

    public final MidiCenter load(final Path path) throws InvalidMidiDataException, IOException {
        sequencer.setSequence(MidiSystem.getSequence(path.toFile()));
        // TODO: rise event!
        return this;
    }

    public final MidiPlayer player() {
        return new MidiPlayer(audience, sequencer);
    }
}
