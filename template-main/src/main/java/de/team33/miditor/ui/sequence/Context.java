package de.team33.miditor.ui.sequence;

import de.team33.midi.Player;
import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

import java.awt.*;

public interface Context {
    Component getFrame();

    Sequence getSequence();

    Player getPlayer();

    Selection<Track> getSelection();

    UIController getTrackHandler();
}
