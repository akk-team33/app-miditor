package de.team33.miditor.ui.sequence;

import de.team33.midi.Part;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.model.Selection;

public interface Context extends de.team33.miditor.ui.Context {

    Selection<Part> selection();

    UIController trackHandler();
}
