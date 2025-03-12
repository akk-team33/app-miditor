package de.team33.miditor.model;

import de.team33.midi.MidiTrack;
import de.team33.midi.Sequence;
import de.team33.selection.SelectionImpl;

public class PartSelection extends SelectionImpl<MidiTrack> {

    public PartSelection(final Sequence sequence) {
        sequence.addListener(Sequence.Channel.SetParts, this::onSetParts);
    }

    private void onSetParts(final Sequence sequence) {
        clear();
    }
}
