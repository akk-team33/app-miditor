package de.team33.miditor.ui.track;

import net.team33.midi.Player;
import net.team33.midi.Sequence;
import net.team33.midi.Track;
import de.team33.miditor.controller.UIController;
import net.team33.selection.Selection;

public interface Context {
    int getIndex();

    Sequence getSequence();

    Track getTrack();

    Player getPlayer();

    Selection<Track> getSelection();

    UIController getTrackHandler();
}
