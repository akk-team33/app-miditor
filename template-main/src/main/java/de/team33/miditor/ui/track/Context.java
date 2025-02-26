package de.team33.miditor.ui.track;

import de.team33.midi.Player;
import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

public interface Context {
    int getIndex();

    Sequence getSequence();

    Track getTrack();

    Player getPlayer();

    Selection<Track> getSelection();

    UIController getTrackHandler();
}
