package de.team33.miditor.model;

import de.team33.midi.Part;
import de.team33.midi.Score;
import de.team33.selection.SelectionImpl;

public class PartSelection extends SelectionImpl<Part> {

    public PartSelection(final Score sequence) {
        sequence.registry().add(Score.Channel.SetTracks, this::onSetParts);
    }

    private void onSetParts(final Score sequence) {
        clear();
    }
}
