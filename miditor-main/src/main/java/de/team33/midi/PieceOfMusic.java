package de.team33.midi;

import de.team33.patterns.mutable.alpha.Mutable;
import de.team33.patterns.notes.beta.Sender;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.UnaryOperator;

import static de.team33.midi.Util.CNV;

public final class PieceOfMusic extends Sender<PieceOfMusic> {

    private static final UnaryOperator<Path> NORMALIZER = path -> path.toAbsolutePath().normalize();

    private final Mutable<Path> path;
    private final FullScore fullScore;
    private final Player player;

    private PieceOfMusic(final Path path, final Executor executor) throws InvalidMidiDataException, IOException {
        super(PieceOfMusic.class, executor, Channel.VALUES);

        final Sequence sequence = MidiSystem.getSequence(path.toFile());
        final Sequencer sequencer = CNV.get(() -> MidiSystem.getSequencer(false));

        this.path = new Mutable<>(NORMALIZER, path);
        this.fullScore = new FullScore(sequence, executor);
        this.player = new Player(sequencer, sequence, executor);

        fullScore.registry().add(FullScore.Channel.SetTracks, any -> player.onSetParts());
    }

    public static Loader loader(final Executor executor) {
        return path -> new PieceOfMusic(path, executor);
    }

    public final Path path() {
        return path.get();
    }

    public final PieceOfMusic setPath(final Path path) {
        this.path.set(path);
        return fire(Channel.SET_PATH);
    }

    public final FullScore fullScore() {
        return fullScore;
    }

    public final Player player() {
        return player;
    }

    public final PieceOfMusic save() throws IOException {
        final Sequence sequence = fullScore.sequence();
        synchronized (sequence) {
            final int mode = (1 < sequence.getTracks().length) ? 1 : 0;
            MidiSystem.write(sequence, mode, path.get().toFile());
        }
        fullScore.resetModified();
        return this;
    }

    @SuppressWarnings({"ParameterHidesMemberVariable", "UnusedReturnValue"})
    public final PieceOfMusic saveAs(final Path path) throws IOException {
        return setPath(path).save();
    }

    @FunctionalInterface
    public interface Channel<M> extends Sender.Channel<PieceOfMusic, M> {

        Channel<Path> SET_PATH = PieceOfMusic::path;

        @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
        Set<Channel<?>> VALUES = Set.of(SET_PATH);
    }

    @FunctionalInterface
    public interface Loader {

        PieceOfMusic load(Path path) throws InvalidMidiDataException, IOException;
    }
}
