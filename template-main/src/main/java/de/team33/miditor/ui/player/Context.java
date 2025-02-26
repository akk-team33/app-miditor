package de.team33.miditor.ui.player;

import de.team33.midi.Player;

import java.awt.*;

public interface Context {
    Player getPlayer();

    Window getWindow();
}
