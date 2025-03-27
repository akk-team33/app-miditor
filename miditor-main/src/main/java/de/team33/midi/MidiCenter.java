package de.team33.midi;

import de.team33.patterns.execution.metis.SimpleAsyncExecutor;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executor;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class MidiCenter {

    private final MidiSequence sequence; // --> "composition"
    private final MidiPlayer sequencer;  // --> "player"

    public MidiCenter(final Path path) throws InvalidMidiDataException, IOException {
        final Executor executor = new SimpleAsyncExecutor();
        this.sequence = MidiSequence.loader(executor).load(path);
        this.sequencer = new MidiPlayer(sequence, executor);
    }

    public final MidiSequence sequence() {
        return sequence;
    }

    public final MidiPlayer sequencer() {
        return sequencer;
    }
}
