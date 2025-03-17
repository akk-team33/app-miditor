package de.team33.midi;

import de.team33.midix.Timing;
import de.team33.patterns.mutable.alpha.Mutable;
import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Mapping;
import de.team33.patterns.notes.alpha.Sender;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.team33.midix.Midi.MetaMessage.Type.SET_TEMPO;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "ClassNamePrefixedWithPackageName", "ClassWithTooManyMethods"})
public class MidiSequence extends Sender<MidiSequence> {

    private static final UnaryOperator<Path> NORMALIZER = path -> path.toAbsolutePath().normalize();

    private final Audience audience;
    private final Mapping mapping;
    private final Sequence backing;
    private final Mutable<Path> path;
    private final ModificationCenter modificationCenter;
    private final MidiTrack.Factory trackFactory;
    private final Features features = new Features();

    MidiSequence(final Path path, final Sequence backing, final Executor executor) {
        super(MidiSequence.class);
        this.audience = new Audience(executor);
        this.mapping = Mapping.builder()
                              .put(Channel.SetPath, () -> this)
                              .put(Channel.SetModified, () -> this)
                              .put(Channel.SetTracks, () -> this)
                              .build();
        this.backing = backing;
        this.path = new Mutable<>(NORMALIZER, path);
        this.modificationCenter = new ModificationCenter(executor);
        this.trackFactory = MidiTrack.factory(modificationCenter, executor);

        modificationCenter.registry()
                          .add(ModificationCenter.Channel.MODIFIED, this::onModified)
                          .add(ModificationCenter.Channel.RESET, this::onModified)
                          .add(ModificationCenter.Channel.SUB_MODIFIED, this::onSubModified);
    }

    public static Loader loader(final Executor executor) {
        return path -> {
            final Sequence backing = MidiSystem.getSequence(path.toFile());
            return new MidiSequence(path, backing, executor);
        };
    }

    private static Stream<Track> streamOf(final Iterable<? extends MidiTrack> tracks) {
        return StreamSupport.stream(tracks.spliterator(), false)
                            .map(MidiTrack::backing);
    }

    private void onModified(final Void unused) {
        fire(Channel.SetModified);
    }

    private void onSubModified(final int id) {
        modificationCenter.increment();
    }

    @Override
    protected final Audience audience() {
        return audience;
    }

    @Override
    protected final Mapping mapping() {
        return mapping;
    }

    public final MidiSequence save() throws IOException {
        synchronized (backing) {
            final int mode = (1 < backing.getTracks().length) ? 1 : 0;
            MidiSystem.write(backing, mode, path.get().toFile());
        }
        return resetModified();
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    public final MidiSequence saveAs(final Path path) throws IOException {
        this.path.set(path);
        return save().fire(Channel.SetPath);
    }

    final Sequence backing() {
        return backing;
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final MidiSequence create(final MidiEvent... events) {
        return create(Arrays.asList(events));
    }

    public final MidiSequence create(final Iterable<? extends MidiEvent> events) {
        synchronized (backing) {
            createTrack(events);
        }
        return setModified();
    }

    private Track newTrack() {
        final Track result = backing.createTrack();
        modificationCenter.increment(Util.idCode(result));
        return result;
    }

    private void createTrack(final Iterable<? extends MidiEvent> events) {
        final Track track = newTrack();
        for (final MidiEvent event : events) {
            track.add(event);
        }
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public final MidiSequence delete(final MidiTrack... tracks) {
        return delete(Arrays.asList(tracks));
    }

    public final MidiSequence delete(final Iterable<? extends MidiTrack> tracks) {
        synchronized (backing) {
            for (final MidiTrack track : tracks) {
                backing.deleteTrack(track.backing());
            }
        }
        return setModified();
    }

    public final MidiSequence join(final Iterable<? extends MidiTrack> tracks) {
        synchronized (backing) {
            final Track newTrack = newTrack();
            streamOf(tracks).flatMap(Util::stream)
                            .forEach(newTrack::add);
            streamOf(tracks).forEach(backing::deleteTrack);
        }
        return setModified();
    }

    public final MidiSequence split(final MidiTrack track) {
        if (getTracks().contains(track)) {
            final Map<Integer, List<MidiEvent>> extracted = track.extractChannels();
            synchronized (backing) {
                for (final List<MidiEvent> events : extracted.values()) {
                    createTrack(events);
                }
            }
            return setModified();
        } else {
            throw new IllegalArgumentException("<track> is not part of this sequence");
        }
    }

    public final List<MidiTrack> getTracks() {
        return features.get(Key.TRACKS);
    }

    public final boolean isModified() {
        return 0L != modificationCenter.get();
    }

    private MidiSequence setModified() {
        features.peek(Key.TRACKS)
                .orElseGet(List::of)
                .forEach(MidiTrack::release);
        features.reset();
        modificationCenter.increment();
        modificationCenter.keep(Util.stream(backing).map(Util::idCode).collect(Collectors.toSet()));
        return fire(Channel.SetTracks);
    }

    private MidiSequence resetModified() {
        modificationCenter.reset();
        return this;
    }

    public final Path getPath() {
        return path.get();
    }

    public final int getTempo() {
        return features.get(Key.TEMPO);
    }

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
            for (int i = 0; i < data.length; ++i) {
                //noinspection NumericCastThatLosesPrecision,MagicNumber
                data[2 - i] = (byte) (mpqn & 0xff);
                mpqn >>= 8;
            }
            track.add(new MidiEvent(SET_TEMPO.newMessage(data), 0L));
        }
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
    public enum Channel implements de.team33.patterns.notes.alpha.Channel<MidiSequence> {
        SetModified,
        SetPath,
        SetTracks
    }

    public interface Loader {
        MidiSequence load(final Path path) throws InvalidMidiDataException, IOException;
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

        @SuppressWarnings("ReturnOfInnerClass")
        @Override
        protected final Features host() {
            return this;
        }

        private List<MidiTrack> newTrackList() {
            synchronized (backing) {
                final Track[] tracks = backing.getTracks();
                return IntStream.range(0, tracks.length)
                                .mapToObj(index -> trackFactory.create(index, tracks[index]))
                                .toList();
            }
        }

        @SuppressWarnings({"NumericCastThatLosesPrecision", "MagicNumber"})
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
