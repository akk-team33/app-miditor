package de.team33.miditor.model;

import de.team33.midi.FullScore;
import de.team33.midi.MidiTrack;
import de.team33.selection.SelectionImpl;

public class PartSelection extends SelectionImpl<MidiTrack> {

    public PartSelection(final FullScore sequence) {
        sequence.registry().add(FullScore.Channel.SetTracks, this::onSetParts);
    }

    private void onSetParts(final FullScore sequence) {
        clear();
    }
}
