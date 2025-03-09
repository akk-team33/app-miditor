package de.team33.midix;

import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class MidiTrack extends Sender<MidiTrack> {

    private final Audience audience;
    private final Mapping mapping;
    private final Track backing;
    private final Features features = new Features();

    MidiTrack(final Executor executor, final Track backing) {
        super(MidiTrack.class);
        this.audience = new Audience(executor);
        this.mapping = Mapping.builder()
                              .build();
        this.backing = backing;
    }

    @Override
    protected final Audience audience() {
        return audience;
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
    }

    public final void add(MidiEvent... var1) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final Map<Integer, List<MidiEvent>> extractChannels() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final MidiEvent get(int var1) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final MidiEvent[] getAll() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final int[] getChannels() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final String getName() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final String getPrefix() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final boolean isModified() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final void remove(MidiEvent... var1) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final void shift(long var1) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final int size() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    public interface Channel<M> extends de.team33.patterns.notes.alpha.Channel<M> {
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<MidiTrack, R> {
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends de.team33.patterns.features.alpha.Features<MidiTrack> {

        private Features() {
            super(ConcurrentHashMap::new);
        }

        @Override
        protected final MidiTrack host() {
            return MidiTrack.this;
        }
    }
}
