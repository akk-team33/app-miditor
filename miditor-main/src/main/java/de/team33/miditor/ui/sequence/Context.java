package de.team33.miditor.ui.sequence;

import de.team33.midi.Music;
import de.team33.midi.Part;
import de.team33.midi.Player;
import de.team33.midi.Score;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

import java.awt.*;

public interface Context {

    Component getFrame();

    Music getMusic();

    Score getSequence();

    Player getPlayer();

    Selection<Part> getSelection();

    UIController getTrackHandler();
}
