package de.team33.miditor.ui.sequence;

import de.team33.midi.FullScore;
import de.team33.midi.MidiPlayer;
import de.team33.midi.MidiTrack;
import de.team33.midi.PieceOfMusic;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

import java.awt.*;

public interface Context {

    Component getFrame();

    PieceOfMusic getMusic();

    FullScore getSequence();

    MidiPlayer getPlayer();

    Selection<MidiTrack> getSelection();

    UIController getTrackHandler();
}
