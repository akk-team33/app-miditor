package de.team33.miditor.ui.track;

import de.team33.midi.FullScore;
import de.team33.midi.MidiPlayer;
import de.team33.midi.MidiTrack;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

public interface Context {
    int getIndex();

    FullScore getSequence();

    MidiTrack getTrack();

    MidiPlayer getPlayer();

    Selection<MidiTrack> getSelection();

    UIController getTrackHandler();
}
