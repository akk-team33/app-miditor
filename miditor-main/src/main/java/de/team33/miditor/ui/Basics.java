package de.team33.miditor.ui;

import de.team33.sphinx.alpha.visual.JButtons;

import javax.swing.*;

public class Basics {

    public static JButtons.Builder<?> jButtonBuilder() {
        return JButtons.builder();
    }

    public static JButtons.Builder<?> jButtonBuilder(final String text) {
        return jButtonBuilder().setText(text);
    }

    public static JButtons.Builder<?> jButtonBuilder(final Icon icon) {
        return jButtonBuilder().setIcon(icon);
    }
}
