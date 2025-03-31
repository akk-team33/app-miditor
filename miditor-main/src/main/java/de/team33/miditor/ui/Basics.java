package de.team33.miditor.ui;

import de.team33.sphinx.alpha.visual.JButtons;

import java.awt.*;

public class Basics {

    public static final Insets INSETS_1111 = new Insets(1, 1, 1, 1);

    public static JButtons.Builder<?> buttonBuilder() {
        return JButtons.builder()
                       .setMargin(INSETS_1111);
    }
}
