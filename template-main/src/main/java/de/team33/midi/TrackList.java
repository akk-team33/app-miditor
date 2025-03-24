package de.team33.midi;

import de.team33.patterns.notes.alpha.Audience;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

class TrackList {

    private final Executor executor;
    private final Sequence sequence;
    private final Map<Track, Entry> backing;

    TrackList(final Sequence sequence, final Executor executor) {
        this.executor = executor;
        this.sequence = sequence;
        this.backing = Util.stream(sequence)
                           .map(this::newEntry)
                           .collect(HashMap::new, TrackList::put, Map::putAll);
    }

    private static void put(final Map<Track, Entry> map, final Entry entry) {
        map.put(entry.track(), entry);
    }

    private Entry newEntry(final Track track) {
        return new Entry(track, new Audience(executor), new AtomicLong(0));
    }

    final Executor executor() {
        return executor;
    }

    final Sequence sequence() {
        return sequence;
    }

    final Track create() {
        final Track track;
        synchronized (sequence) {
            track = sequence.createTrack();
            backing.put(track, newEntry(track));
        }
        return track;
    }

    final List<Track> delete(final Collection<? extends Track> tracks) {
        final List<Track> result = new ArrayList<>(tracks.size());
        synchronized (sequence) {
            for (final Track track : tracks) {
                if (sequence.deleteTrack(track)) {
                    backing.remove(track);
                    result.add(track);
                }
            }
        }
        return result;
    }

    final List<Track> tracks() {
        synchronized (sequence) {
            return List.of(sequence.getTracks());
        }
    }

    /**
     * Returns the index of specified element in <em>this</em> list,
     * or -1 if this list does not contain the element
     */
    final int indexOf(final Track track) {
        synchronized (sequence) {
            return Arrays.asList(sequence.getTracks())
                         .indexOf(track);
        }
    }

    final Entry entryOf(final Track track) {
        synchronized (sequence) {
            return Optional.ofNullable(backing.get(track))
                           .orElseThrow();
        }
    }

    record Entry(Track track, Audience audience, AtomicLong modCounter) {}
}
