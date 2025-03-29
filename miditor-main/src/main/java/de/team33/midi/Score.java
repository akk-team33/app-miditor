package de.team33.midi;

import de.team33.patterns.lazy.narvi.LazyFeatures;
import de.team33.patterns.notes.beta.Sender;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.team33.midi.Util.MetaMessage.Type.SET_TEMPO;

@SuppressWarnings("UnusedReturnValue")
public class Score extends Sender<Score> {

    private static final List<MidiEvent> EMPTY = List.of();

    private final Parts parts;
    private final Part.Factory trackFactory;
    private final AtomicLong modCounter = new AtomicLong();
    private final Features features = new Features();

    Score(final Sequence backing, final Executor executor) {
        super(Score.class, executor, Channel.VALUES);
        this.parts = new Parts(backing, executor, this::onModifiedTrack);
        this.trackFactory = Part.factory(parts);
    }

    private void onModifiedTrack() {
        modCounter.incrementAndGet();
        fire(Channel.SetModified);
    }

    private static Stream<Track> streamOf(final Iterable<Part> tracks) {
        return StreamSupport.stream(tracks.spliterator(), false)
                            .map(Part::backing);
    }

    final Sequence sequence() {
        return parts.sequence();
    }

    public final Score create(final Iterable<? extends MidiEvent> events) {
        createBase(events);
        return setModified();
    }

    private void createBase(final Iterable<? extends MidiEvent> events) {
        final Track track = parts.create();
        for (final MidiEvent event : events) {
            track.add(event);
        }
    }

    public final Score delete(final Collection<Part> tracks) {
        parts.delete(tracks.stream().map(Part::backing).toList());
        return setModified();
    }

    public final Score join(final Collection<Part> tracks) {
        final Track track = parts.create();
        streamOf(tracks).flatMap(Util::stream)
                        .forEach(track::add);
        return delete(tracks);
    }

    public final Score split(final Part track) {
        final Map<Integer, List<MidiEvent>> extracted = track.extractChannels();
        for (final List<MidiEvent> events : extracted.values()) {
            createBase(events);
        }
        return setModified();
    }

    public final List<Part> getTracks() {
        return features.get(Key.TRACKS);
    }

    public final boolean isModified() {
        return 0L != modCounter.get();
    }

    private Score setModified() {
        features.reset();
        modCounter.incrementAndGet();
        return fire(Channel.SetTracks, Channel.SetModified);
    }

    final Score resetModified() {
        modCounter.set(0);
        features.get(Key.TRACKS).forEach(Part::resetModified);
        return fire(Channel.SetModified);
    }

    @SuppressWarnings("WeakerAccess")
    public final int getTempo() {
        return features.get(Key.TEMPO);
    }

    @SuppressWarnings("unused")
    final void setTempo(final int tempo) {
        if (getTracks().isEmpty()) {
            create(EMPTY);
        }
        final Part track = getTracks().get(0);
        track.remove(Util.stream(track)
                         .filter(SET_TEMPO::isTypeOf)
                         .filter(event -> 0L == event.getTick())
                         .toList());

        if (0 < tempo) {
            final byte[] data = new byte[3];
            long microsecondsPerMidiQuarterNote = Math.round(Util.MICROSECONDS_PER_MINUTE / tempo);
            for (int i = data.length - 1; i >= 0; --i) {
                //noinspection MagicNumber,NumericCastThatLosesPrecision
                data[i] = (byte) (microsecondsPerMidiQuarterNote & 0xff);
                microsecondsPerMidiQuarterNote >>= 8;
            }
            track.add(new MidiEvent(SET_TEMPO.newMessage(data), 0L));
        }
    }

    public final long getTickLength() {
        synchronized (sequence()) {
            return sequence().getTickLength();
        }
    }

    public final Timing getTiming() {
        return features.get(Key.TIMING);
    }

    @FunctionalInterface
    @SuppressWarnings("ClassNameSameAsAncestorName")
    public interface Channel extends Sender.Channel<Score, Score> {

        Channel SetModified = score -> score;
        Channel SetTracks = score -> score;

        @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
        Set<Channel> VALUES = Set.of(SetModified, SetTracks);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends LazyFeatures.Key<Features, R> {

        Key<List<Part>> TRACKS = Features::newTrackList;
        Key<Integer> TEMPO = Features::newTempo;
        Key<Timing> TIMING = Features::newTiming;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends LazyFeatures<Features> {

        @Override
        protected final Features host() {
            return this;
        }

        private List<Part> newTrackList() {
            return parts.tracks().stream()
                        .map(trackFactory::create)
                        .toList();
        }

        private int newTempo() {
            return parts.tracks().stream()
                        .flatMapToInt(Features::newTempo)
                        .findFirst()
                        .orElse(0);
        }

        private static IntStream newTempo(final Track track) {
            return Util.firstTempoEvent(track).stream()
                       .mapToInt(Features::newTempo);
        }

        @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"})
        private static int newTempo(final MidiEvent event) {
            final byte[] bytes = event.getMessage().getMessage();
            int microsecondsPerMidiQuarterNote = 0;
            for (int i = 3; 6 > i; ++i) {
                microsecondsPerMidiQuarterNote <<= 8;
                microsecondsPerMidiQuarterNote += (bytes[i] & 0xff);
            }
            return (int) Math.round(Util.MICROSECONDS_PER_MINUTE / microsecondsPerMidiQuarterNote);
        }

        private Timing newTiming() {
            return parts.tracks().stream().findFirst()
                        .flatMap(this::newTiming)
                        .orElseGet(() -> Timing.of(sequence()));
        }

        private Optional<Timing> newTiming(final Track track) {
            return Util.firstTimeSignature(track)
                       .map(MidiEvent::getMessage)
                       .map(message -> Timing.of(message, sequence()));
        }
    }
}
