package de.team33.miditor.model;

import de.team33.midi.MidiSequence;
import de.team33.midi.MidiTrack;
import de.team33.selection.SelectionImpl;

public class PartSelection extends SelectionImpl<MidiTrack> {

    public PartSelection(final MidiSequence sequence) {
        sequence.add(MidiSequence.Channel.SetTracks, this::onSetParts);
    }

    private void onSetParts(final MidiSequence sequence) {
        clear();
    }
}
