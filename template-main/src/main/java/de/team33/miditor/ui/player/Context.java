package de.team33.miditor.ui.player;

import java.awt.Window;
import de.team33.midi.Player;

public interface Context {
    Player getPlayer();

    Window getWindow();
}
