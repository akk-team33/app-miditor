package de.team33.midix;

import de.team33.patterns.notes.beta.Sender;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Executor;

import static de.team33.midix.Util.CNV;

public class MidiCenter extends Sender<MidiCenter> {

    private final Executor executor;
    private final Sequencer sequencer;
    private final MidiPlayer midiPlayer;
    private final Mutable<Path> filePath;

    public MidiCenter(final Executor executor) {
        super(MidiCenter.class, executor, Set.of(Channel.SET_PATH));
        this.executor = executor;
        this.sequencer = CNV.get(() -> MidiSystem.getSequencer(true));
        this.midiPlayer = new MidiPlayer(executor, sequencer);
        this.filePath = new Mutable<Path>(p -> p.toAbsolutePath().normalize()).set(Path.of("no-name.mid"));
    }

    public final MidiCenter load(final Path path) throws InvalidMidiDataException, IOException {
        final File file = filePath.set(path).get().toFile();
        sequencer.setSequence(MidiSystem.getSequence(file));
        fire(Channel.SET_PATH);
        return this;
    }

    public final MidiPlayer player() {
        return midiPlayer;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    public interface Channel<M> extends Sender.Channel<MidiCenter, M> {

        Channel<Path> SET_PATH = midiCenter -> midiCenter.filePath.get();
    }
}
