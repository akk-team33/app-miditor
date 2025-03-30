package de.team33.miditor.ui.track;

import de.team33.midi.Part;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.model.Selection;

public interface Context extends de.team33.miditor.ui.Context {

    int index();

    Part part();

    Selection<Part> selection();

    UIController partHandler();
}
