package de.team33.miditor.ui;

import de.team33.sphinx.alpha.visual.JButtons;

import javax.swing.*;
import java.awt.*;

public final class Basics {

    public static final Insets INSETS_1111 = new Insets(1, 1, 1, 1);

    private Basics() {
    }

    public static JButtons.Builder<JButton> iconButton(final Icon icon) {
        return JButtons.builder()
                       .setIcon(icon)
                       .setMargin(INSETS_1111);
    }

    public static void setFont(final JComponent component, final int style, final int size) {
        final String name = component.getFont().getName();
        component.setFont(new Font(name, style, size));
    }
}
