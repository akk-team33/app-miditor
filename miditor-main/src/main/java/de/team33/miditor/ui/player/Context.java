package de.team33.miditor.ui.player;

import de.team33.midi.MidiPlayer;
import de.team33.midi.MidiSequence;
import de.team33.midix.Timing;

import java.awt.*;

public interface Context {

    Timing getTiming();

    MidiSequence getSequence();

    MidiPlayer getPlayer();

    Window getWindow();
}
