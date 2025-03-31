package de.team33.miditor.ui;

import de.team33.midi.Music;
import de.team33.midi.Player;
import de.team33.midi.Score;
import de.team33.midi.Timing;

import java.awt.*;

public interface Context {

    Window window();

    Music music();

    default Player player() {
        return music().player();
    }

    default Score score() {
        return music().score();
    }

    default Timing timing() {
        return score().getTiming();
    }
}
