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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class SequenceProxy {

    private final Sequence backing;
    private final AtomicBoolean modification = new AtomicBoolean(false);
    private final Features features = new Features();

    public SequenceProxy(final Sequence backing) {
        this.backing = backing;
    }

    private static Stream<Track> streamOf(final Iterable<? extends MidiTrack> tracks) {
        return StreamSupport.stream(tracks.spliterator(), false)
                            .map(MidiTrack::backing);
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

    @SuppressWarnings("OverloadedVarargsMethod")
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

    public final SequenceProxy join(final Iterable<? extends MidiTrack> tracks) {
        synchronized (backing) {
            final Track newTrack = backing.createTrack();
            streamOf(tracks).flatMap(Util::stream)
                            .forEach(newTrack::add);
            streamOf(tracks).forEach(backing::deleteTrack);
        }
        return setModified(true);
    }

    public final List<MidiTrack> getTracks() {
        return features.get(Key.TRACKS);
    }

    public final boolean isModified() {
        return modification.get();
    }

    private SequenceProxy setModified(final boolean modified) {
        modification.set(modified);
        features.reset();
        // TODO!: return fire(MidiTrack.Internal.SetModified, MidiTrack.Channel.SetEvents);
        return this;
    }

    public final int getTempo() {
        return features.get(Key.TEMPO);
    }

    public final long getTickLength() {
        synchronized (backing) {
            return backing.getTickLength();
        }
    }

    public final Timing getTiming() {
        return features.get(Key.TIMING);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<Features, R> {

        Key<List<MidiTrack>> TRACKS = Features::newTrackList;
        Key<Integer> TEMPO = Features::newTempo;
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

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private int newTempo() {
            synchronized (backing) {
                final Track[] tracks = backing.getTracks();
                if (0 < tracks.length) {
                    final MidiEvent event = Util.firstTempoEvent(tracks[0])
                                                .orElse(null);
                    if (null != event) {
                        final byte[] bytes = event.getMessage().getMessage();
                        int mpqn = 0;
                        for (int i = 3; 6 > i; ++i) {
                            mpqn <<= 8;
                            mpqn += (bytes[i] & 0xff);
                        }
                        return (int) Math.round(Util.MSPMQN / mpqn);
                    }
                }
                return 0;
            }
        }

        private Timing newTiming() {
            synchronized (backing) {
                return Util.firstTimeSignature(backing.getTracks()[0])
                           .map(MidiEvent::getMessage)
                           .map(message -> Timing.of(message, backing))
                           .orElseGet(() -> Timing.of(backing));
            }
        }
    }
}
