package de.team33.miditor.backend;

import de.team33.patterns.exceptional.dione.Converter;
import de.team33.patterns.exceptional.dione.Wrapping;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.IOException;
import java.nio.file.Path;

public class MidiCenter {

    static final Converter CNV = Converter.using(Wrapping.method(IllegalStateException::new));

    final Sequencer sequencer;

    public MidiCenter() {
        this.sequencer = CNV.get(() -> MidiSystem.getSequencer(true));
    }

    public final MidiCenter load(final Path path) throws InvalidMidiDataException, IOException {
        final Sequence sequence = MidiSystem.getSequence(path.toFile());
        sequencer.setSequence(sequence);
        // TODO: rise event!
        return this;
    }

    public final MidiPlayer player() {
        return new MidiPlayer() {
            @Override
            MidiCenter center() {
                return MidiCenter.this;
            }
        };
    }
}
