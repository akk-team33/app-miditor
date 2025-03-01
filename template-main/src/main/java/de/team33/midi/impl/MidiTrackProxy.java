package de.team33.midi.impl;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.stream.IntStream;

class MidiTrackProxy {

    // TODO: make private ...
    final Track midiTrack;

    MidiTrackProxy(final Track midiTrack) {
        this.midiTrack = midiTrack;
    }

    final boolean mtAdd(final MidiEvent event) {
        return midiTrack.add(event);
    }

    final boolean mtRemove(final MidiEvent event) {
        return midiTrack.remove(event);
    }

    final MidiEvent mtGet(final int index) {
        return midiTrack.get(index);
    }

    final int size() {
        return midiTrack.size();
    }

    final long ticks() {
        return midiTrack.ticks();
    }

    final int indexOf(final Sequence sequence) {
        final Track[] tracks = sequence.getTracks();
        return IntStream.range(0, tracks.length)
                        .filter(index -> tracks[index] == midiTrack)
                        .findAny()
                        .orElse(-1);
    }
}
