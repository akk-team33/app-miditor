package de.team33.miditor.ui.track;

import de.team33.midi.Part;
import de.team33.midi.Player;
import de.team33.midi.Score;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.model.Selection;

public interface Context {

    de.team33.miditor.ui.sequence.Context backing();

    int index();

    Part part();

    default Score score() {
        return backing().score();
    }

    default Player player() {
        return backing().player();
    }

    default Selection<Part> selection() {
        return backing().selection();
    }

    default UIController trackHandler() {
        return backing().trackHandler();
    }
}
