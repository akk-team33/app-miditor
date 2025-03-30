package de.team33.miditor.ui.sequence;

import de.team33.midi.Part;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.model.Selection;

import java.awt.*;

public interface Context extends de.team33.miditor.ui.Context {

    Window window();

    Selection<Part> selection();

    UIController partHandler();
}
