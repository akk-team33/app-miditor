package de.team33.miditor.ui;

import de.team33.patterns.building.elara.LateBuilder;
import de.team33.sphinx.alpha.visual.JLabels;

import javax.swing.*;

public class XSpinner extends JLabel {

    public static Builder builder() {
        return new Builder();
    }

    @FunctionalInterface
    public interface Setup<S extends Setup<S>> extends JLabels.Setup<XSpinner, S> {
    }

    public static final class Builder extends LateBuilder<XSpinner, Builder> implements Setup<Builder> {

        private Builder() {
            super(XSpinner::new, Builder.class);
        }
    }
}
