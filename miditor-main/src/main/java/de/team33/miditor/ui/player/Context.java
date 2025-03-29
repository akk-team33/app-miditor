package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.midi.Score;
import de.team33.midi.Timing;

import java.awt.*;

public interface Context {

    Timing getTiming();

    Score getSequence();

    Player getPlayer();

    Window getWindow();
}
