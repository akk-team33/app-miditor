package de.team33.midi;

import de.team33.midix.Timing;
import de.team33.patterns.lazy.narvi.LazyFeatures;
import de.team33.patterns.notes.beta.Sender;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.Arrays;
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

import static de.team33.midix.Midi.MetaMessage.Type.SET_TEMPO;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "ClassNamePrefixedWithPackageName"})
public class FullScore extends Sender<FullScore> {

    private final Parts parts;
    private final Part.Factory trackFactory;
    private final AtomicLong modCounter = new AtomicLong();
    private final Features features = new Features();

    FullScore(final Sequence backing, final Executor executor) {
        super(FullScore.class, executor, Channel.VALUES);
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

    final Sequence backing() {
        return parts.sequence();
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final FullScore create(final MidiEvent... events) {
        return create(Arrays.asList(events));
    }

    public final FullScore create(final Iterable<? extends MidiEvent> events) {
        createBase(events);
        return setModified();
    }

    private void createBase(final Iterable<? extends MidiEvent> events) {
        final Track track = parts.create();
        for (final MidiEvent event : events) {
            track.add(event);
        }
    }

    public final FullScore delete(final Part... tracks) {
        return delete(Arrays.asList(tracks));
    }

    public final FullScore delete(final Collection<Part> tracks) {
        parts.delete(tracks.stream().map(Part::backing).toList());
        return setModified();
    }

    public final FullScore join(final Collection<Part> tracks) {
        final Track track = parts.create();
        streamOf(tracks).flatMap(Util::stream)
                        .forEach(track::add);
        return delete(tracks);
    }

    public final FullScore split(final Part track) {
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

    private FullScore setModified() {
        features.reset();
        modCounter.incrementAndGet();
        return fire(Channel.SetTracks, Channel.SetModified);
    }

    final FullScore resetModified() {
        modCounter.set(0);
        features.get(Key.TRACKS).forEach(Part::resetModified);
        return fire(Channel.SetModified);
    }

    public final int getTempo() {
        return features.get(Key.TEMPO);
    }

    @SuppressWarnings({"NumericCastThatLosesPrecision", "MagicNumber"})
    final void setTempo(final int tempo) {
        if (getTracks().isEmpty()) {
            create();
        }
        final Part track = getTracks().get(0);
        track.remove(Util.stream(track)
                         .filter(SET_TEMPO::isTypeOf)
                         .filter(event -> 0L == event.getTick())
                         .toList());

        if (0 < tempo) {
            final byte[] data = new byte[3];
            long mpqn = Math.round(Util.MSPMQN / tempo);
            for (int i = data.length - 1; i >= 0; --i) {
                data[i] = (byte) (mpqn & 0xff);
                mpqn >>= 8;
            }
            track.add(new MidiEvent(SET_TEMPO.newMessage(data), 0L));
        }
    }

    public final long getTickLength() {
        synchronized (backing()) {
            return backing().getTickLength();
        }
    }

    public final Timing getTiming() {
        return features.get(Key.TIMING);
    }

    @FunctionalInterface
    @SuppressWarnings("ClassNameSameAsAncestorName")
    public interface Channel extends Sender.Channel<FullScore, FullScore> {

        Channel SetModified = midiSequence -> midiSequence;
        Channel SetTracks = midiSequence -> midiSequence;

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
            int mpqn = 0;
            for (int i = 3; 6 > i; ++i) {
                mpqn <<= 8;
                mpqn += (bytes[i] & 0xff);
            }
            return (int) Math.round(Util.MSPMQN / mpqn);
        }

        private Timing newTiming() {
            return parts.tracks().stream().findFirst()
                        .flatMap(this::newTiming)
                        .orElseGet(() -> Timing.of(backing()));
        }

        private Optional<Timing> newTiming(final Track track) {
            return Util.firstTimeSignature(track)
                       .map(MidiEvent::getMessage)
                       .map(message -> Timing.of(message, backing()));
        }
    }
}
