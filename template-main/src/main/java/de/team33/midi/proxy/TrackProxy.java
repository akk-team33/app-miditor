package de.team33.midi.proxy;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

public class TrackProxy {

    private final Track backing;

    @SuppressWarnings("WeakerAccess")
    public TrackProxy(final Track backing) {
        this.backing = backing;
    }

    public final boolean add(final MidiEvent event) {
        return backing.add(event);
    }

    public final boolean remove(final MidiEvent event) {
        return backing.remove(event);
    }

    public final MidiEvent get(final int index) {
        return backing.get(index);
    }

    public final int size() {
        return backing.size();
    }

    public final long ticks() {
        return backing.ticks();
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof final TrackProxy other) && backing.equals(other.backing));
    }

    @Override
    public final int hashCode() {
        return backing.hashCode();
    }

    final void recordEnable(final Sequencer sequencer, final int channel) {
        sequencer.recordEnable(backing, channel);
    }

    final void recordDisable(final Sequencer sequencer) {
        sequencer.recordDisable(backing);
    }
}
