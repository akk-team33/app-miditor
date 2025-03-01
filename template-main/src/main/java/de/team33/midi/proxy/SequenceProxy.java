package de.team33.midi.proxy;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Patch;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class SequenceProxy {

    private final Sequence backing;
    private transient volatile List<TrackProxy> tracks;

    public SequenceProxy(final Sequence backing) {
        this.backing = backing;
        this.tracks = newTracks(backing);
    }

    private static List<TrackProxy> newTracks(final Sequence backing) {
        return Stream.of(backing.getTracks())
                     .map(TrackProxy::new)
                     .toList();
    }

    public final float getDivisionType() {
        return backing.getDivisionType();
    }

    public final int getResolution() {
        return backing.getResolution();
    }

    public final TrackProxy createTrack() {
        final Track created = backing.createTrack();
        this.tracks = newTracks(backing);
        return new TrackProxy(created);
    }

    public final boolean deleteTrack(final TrackProxy track) {
        final int index = tracks.indexOf(track);
        final Track candidate = backing.getTracks()[index];
        return backing.deleteTrack(candidate);
    }

    public final List<TrackProxy> getTracks() {
        // <tracks> is an immutable List ...
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return tracks;
    }

    public final long getMicrosecondLength() {
        return backing.getMicrosecondLength();
    }

    public final long getTickLength() {
        return backing.getTickLength();
    }

    public final Patch[] getPatchList() {
        return backing.getPatchList();
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof final SequenceProxy other) && backing.equals(other.backing));
    }

    @Override
    public final int hashCode() {
        return backing.hashCode();
    }

    final SequenceProxy setSequencer(final Sequencer sequencer) throws InvalidMidiDataException {
        sequencer.setSequence(backing);
        return this;
    }
}
