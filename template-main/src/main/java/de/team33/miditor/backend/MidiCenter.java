package de.team33.miditor.backend;

import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static de.team33.miditor.backend.Util.CNV;

public class MidiCenter extends Sender<MidiCenter> {

    private final Audience audience;
    private final Mapping mapping;
    private final Sequencer sequencer;
    private final MidiPlayer midiPlayer;
    private final Mutable<Path> filePath;

    public MidiCenter() {
        super(MidiCenter.class);
        this.audience = new Audience();
        this.sequencer = CNV.get(() -> MidiSystem.getSequencer(true));
        this.midiPlayer = new MidiPlayer(audience, sequencer);
        this.filePath = new Mutable<Path>(p -> p.toAbsolutePath().normalize()).set(Path.of("no-name.mid"));
        this.mapping = Mapping.builder()
                              .put(Channel.SET_PATH, filePath::get)
                              .build();
    }

    @Override
    protected final Audience audience() {
        return audience;
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
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
    public interface Channel<M> extends de.team33.patterns.notes.alpha.Channel<M> {

        Channel<Path> SET_PATH = () -> "SET_PATH";
    }
}
