package de.team33.miditor.ui.track;

import de.team33.midi.FullScore;
import de.team33.midi.MidiTrack;
import de.team33.midi.Player;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

public interface Context {
    int getIndex();

    FullScore getSequence();

    MidiTrack getTrack();

    Player getPlayer();

    Selection<MidiTrack> getSelection();

    UIController getTrackHandler();
}
