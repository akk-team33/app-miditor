package de.team33.miditor.ui.track;

import de.team33.midi.MidiPlayer;
import de.team33.midi.MidiSequence;
import de.team33.midi.MidiTrack;
import de.team33.miditor.controller.UIController;
import de.team33.selection.Selection;

public interface Context {
    int getIndex();

    MidiSequence getSequence();

    MidiTrack getTrack();

    MidiPlayer getPlayer();

    Selection<MidiTrack> getSelection();

    UIController getTrackHandler();
}
