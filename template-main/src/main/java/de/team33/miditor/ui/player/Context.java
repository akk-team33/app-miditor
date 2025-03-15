package de.team33.miditor.ui.player;

import de.team33.midi.MidiPlayer;

import java.awt.*;

public interface Context {
    MidiPlayer getPlayer();

    Window getWindow();
}
