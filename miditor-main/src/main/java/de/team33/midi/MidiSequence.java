package de.team33.midi;

import de.team33.midix.Timing;
import de.team33.patterns.lazy.narvi.LazyFeatures;
import de.team33.patterns.mutable.alpha.Mutable;
import de.team33.patterns.notes.beta.Sender;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.team33.midix.Midi.MetaMessage.Type.SET_TEMPO;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "ClassNamePrefixedWithPackageName", "ClassWithTooManyMethods"})
public class MidiSequence extends Sender<MidiSequence> {

    private static final UnaryOperator<Path> NORMALIZER = path -> path.toAbsolutePath().normalize();

    private final TrackList trackList;
    private final Mutable<Path> path;
    private final MidiTrack.Factory trackFactory;
    private final AtomicLong modCounter = new AtomicLong();
    private final Features features = new Features();

    MidiSequence(final Path path, final Sequence backing, final Executor executor) {
        super(MidiSequence.class, executor, Channel.VALUES);
        this.trackList = new TrackList(backing, executor, this::onModifiedTrack);
        this.path = new Mutable<>(NORMALIZER, path);
        this.trackFactory = MidiTrack.factory(trackList);
    }

    private void onModifiedTrack() {
        modCounter.incrementAndGet();
        fire(Channel.SetModified);
    }

    public static Loader loader(final Executor executor) {
        return path -> {
            final Sequence backing = MidiSystem.getSequence(path.toFile());
            return new MidiSequence(path, backing, executor);
        };
    }

    private static Stream<Track> streamOf(final Iterable<MidiTrack> tracks) {
        return StreamSupport.stream(tracks.spliterator(), false)
                            .map(MidiTrack::backing);
    }

    public final MidiSequence save() throws IOException {
        synchronized (backing()) {
            final int mode = (1 < backing().getTracks().length) ? 1 : 0;
            MidiSystem.write(backing(), mode, path.get().toFile());
        }
        return resetModified();
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    public final MidiSequence saveAs(final Path path) throws IOException {
        this.path.set(path);
        return save().fire(Channel.SetPath);
    }

    final Sequence backing() {
        return trackList.sequence();
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final MidiSequence create(final MidiEvent... events) {
        return create(Arrays.asList(events));
    }

    public final MidiSequence create(final Iterable<? extends MidiEvent> events) {
        createBase(events);
        return setModified();
    }

    private void createBase(final Iterable<? extends MidiEvent> events) {
        final Track track = trackList.create();
        for (final MidiEvent event : events) {
            track.add(event);
        }
    }

    public final MidiSequence delete(final MidiTrack... tracks) {
        return delete(Arrays.asList(tracks));
    }

    public final MidiSequence delete(final Collection<MidiTrack> tracks) {
        trackList.delete(tracks.stream().map(MidiTrack::backing).toList());
        return setModified();
    }

    public final MidiSequence join(final Collection<MidiTrack> tracks) {
        final Track track = trackList.create();
        streamOf(tracks).flatMap(Util::stream)
                        .forEach(track::add);
        return delete(tracks);
    }

    public final MidiSequence split(final MidiTrack track) {
        final Map<Integer, List<MidiEvent>> extracted = track.extractChannels();
        for (final List<MidiEvent> events : extracted.values()) {
            createBase(events);
        }
        return setModified();
    }

    public final List<MidiTrack> getTracks() {
        return features.get(Key.TRACKS);
    }

    public final boolean isModified() {
        return 0L != modCounter.get();
    }

    private MidiSequence setModified() {
        features.reset();
        modCounter.incrementAndGet();
        return fire(Channel.SetTracks, Channel.SetModified);
    }

    private MidiSequence resetModified() {
        modCounter.set(0);
        features.get(Key.TRACKS).forEach(MidiTrack::resetModified);
        return fire(Channel.SetModified);
    }

    public final Path getPath() {
        return path.get();
    }

    public final int getTempo() {
        return features.get(Key.TEMPO);
    }

    @SuppressWarnings({"NumericCastThatLosesPrecision", "MagicNumber"})
    final void setTempo(final int tempo) {
        if (getTracks().isEmpty()) {
            create();
        }
        final MidiTrack track = getTracks().get(0);
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
    public interface Channel extends Sender.Channel<MidiSequence, MidiSequence> {

        Channel SetModified = midiSequence -> midiSequence;
        Channel SetPath = midiSequence -> midiSequence;
        Channel SetTracks = midiSequence -> midiSequence;

        @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
        Set<Channel> VALUES = Set.of(SetModified, SetPath, SetTracks);
    }

    @FunctionalInterface
    public interface Loader {
        MidiSequence load(final Path path) throws InvalidMidiDataException, IOException;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends LazyFeatures.Key<Features, R> {

        Key<List<MidiTrack>> TRACKS = Features::newTrackList;
        Key<Integer> TEMPO = Features::newTempo;
        Key<Timing> TIMING = Features::newTiming;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends LazyFeatures<Features> {

        @Override
        protected final Features host() {
            return this;
        }

        private List<MidiTrack> newTrackList() {
            return trackList.tracks().stream()
                            .map(trackFactory::create)
                            .toList();
        }

        private int newTempo() {
            return trackList.tracks().stream()
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
            return trackList.tracks().stream().findFirst()
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
