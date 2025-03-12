package de.team33.midi.testing;

import de.team33.testing.io.hydra.FileIO;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.IOException;
import java.nio.file.Path;

public class MidiTestBase {

    private static final String RESPECT_MID_NAME = "Respect.mid";
    private static final Path TEST_PATH = Path.of("target", "testing", "midi", "files");
    private static final Path RESPECT_MID = TEST_PATH.resolve(RESPECT_MID_NAME);

    private final Sequence sequence;

    protected MidiTestBase() throws InvalidMidiDataException, IOException {
        sequence = loadSequence();
    }

    private static Sequence loadSequence() throws InvalidMidiDataException, IOException {
        FileIO.copy(MidiTestBase.class, RESPECT_MID_NAME, RESPECT_MID);
        return MidiSystem.getSequence(RESPECT_MID.toFile());
    }

    protected final Sequence sequence() {
        return sequence;
    }
}