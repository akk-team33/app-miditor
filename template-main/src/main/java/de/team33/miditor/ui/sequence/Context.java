package de.team33.miditor.ui.sequence;

import java.awt.Component;
import de.team33.midi.Player;
import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;
import net.team33.selection.Selection;

public interface Context {
    Component getFrame();

    Sequence getSequence();

    Player getPlayer();

    Selection<Track> getSelection();

    UIController getTrackHandler();
}
