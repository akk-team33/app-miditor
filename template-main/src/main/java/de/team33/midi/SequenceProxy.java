package de.team33.midi;

import de.team33.midix.Timing;

import javax.sound.midi.Patch;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SequenceProxy {

    private final Sequence backing;
    private final Features features = new Features();

    public SequenceProxy(final Sequence backing) {
        this.backing = backing;
    }

    private SequenceProxy setModified(final boolean modified) {
        // TODO?: modification.set(modified);
        features.reset();
        // TODO!: return fire(MidiTrack.Internal.SetModified, MidiTrack.Channel.SetEvents);
        return this;
    }

    private Stream<Track> stream() {
        return Stream.of(backing.getTracks());
    }

    public final Timing timing() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final SequenceProxy delete(final MidiTrack track) {
        synchronized (backing) {
            backing.deleteTrack(track.backing());
        }
        return setModified(true);
    }

    public Track createTrack() {
        return backing.createTrack();
    }

    public List<MidiTrack> tracks() {
        return features.get(Key.TRACKS);
    }

    public long getMicrosecondLength() {
        return backing.getMicrosecondLength();
    }

    public long getTickLength() {
        return backing.getTickLength();
    }

    public Patch[] getPatchList() {
        return backing.getPatchList();
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<Features, R> {

        Key<List<MidiTrack>> TRACKS = Features::newTrackList;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends de.team33.patterns.features.alpha.Features<Features> {

        private Features() {
            super(ConcurrentHashMap::new);
        }

        @Override
        protected final Features host() {
            return this;
        }

        private List<MidiTrack> newTrackList() {
            synchronized (backing) {
                final Track[] tracks = backing.getTracks();
                return IntStream.range(0, tracks.length)
                                .mapToObj(index -> new MidiTrack(index, tracks[index]))
                                .toList();
            }
        }
    }
}
