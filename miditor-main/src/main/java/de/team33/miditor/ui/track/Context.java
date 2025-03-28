package de.team33.miditor.ui.track;

import de.team33.midi.FullScore;
import de.team33.midi.Part;
import de.team33.midi.Player;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

public interface Context {
    int getIndex();

    FullScore getSequence();

    Part getTrack();

    Player getPlayer();

    Selection<Part> getSelection();

    UIController getTrackHandler();
}
