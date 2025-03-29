package de.team33.miditor.ui.player;

import de.team33.midi.FullScore;
import de.team33.midi.Player;
import de.team33.midi.Timing;

import java.awt.*;

public interface Context {

    Timing getTiming();

    FullScore getSequence();

    Player getPlayer();

    Window getWindow();
}
