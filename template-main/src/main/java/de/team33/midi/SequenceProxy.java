package de.team33.midi;

import de.team33.midix.Timing;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class SequenceProxy {

    private final Sequence backing;
    private final AtomicBoolean modification = new AtomicBoolean(false);
    private final Features features = new Features();

    public SequenceProxy(final Sequence backing) {
        this.backing = backing;
    }

    private SequenceProxy setModified(final boolean modified) {
        modification.set(modified);
        features.reset();
        // TODO!: return fire(MidiTrack.Internal.SetModified, MidiTrack.Channel.SetEvents);
        return this;
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final SequenceProxy create(final MidiEvent... events) {
        return create(Arrays.asList(events));
    }

    public final SequenceProxy create(final Iterable<? extends MidiEvent> events) {
        synchronized (backing) {
            final Track track = backing.createTrack();
            for (final MidiEvent event : events) {
                track.add(event);
            }
        }
        return setModified(true);
    }

    public final SequenceProxy delete(final MidiTrack... tracks) {
        return delete(Arrays.asList(tracks));
    }

    public final SequenceProxy delete(final Iterable<? extends MidiTrack> tracks) {
        synchronized (backing) {
            for (final MidiTrack track : tracks) {
                backing.deleteTrack(track.backing());
            }
        }
        return setModified(true);
    }

    public final List<MidiTrack> tracks() {
        return features.get(Key.TRACKS);
    }

    public final long getTickLength() {
        synchronized (backing) {
            return backing.getTickLength();
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<Features, R> {

        Key<List<MidiTrack>> TRACKS = Features::newTrackList;
        Key<Timing> TIMING = Features::newTiming;
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

        private Timing newTiming() {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }
}
