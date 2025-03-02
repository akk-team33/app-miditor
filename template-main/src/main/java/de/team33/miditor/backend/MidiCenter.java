package de.team33.miditor.backend;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.IOException;
import java.nio.file.Path;

public class MidiCenter {

    private final Sequencer sequencer;
    private Sequence sequence;

    public MidiCenter() {
        try {
            this.sequencer = MidiSystem.getSequencer(true);
            //this.sequencer.open();
        } catch (final MidiUnavailableException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public final MidiCenter load(final Path path) throws InvalidMidiDataException, IOException {
        sequence = MidiSystem.getSequence(path.toFile());
        sequencer.setSequence(sequence);
        // TODO: rise event!
        return this;
    }

    public final MidiPlayer player() {
        return new Player();
    }

    private class Player extends MidiPlayer {
        @Override
        final Sequencer sequencer() {
            return sequencer;
        }
    }
}
