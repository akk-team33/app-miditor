package de.team33.miditor.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public abstract class SmallButton extends JToggleButton implements ActionListener {
    public SmallButton() {
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setFont(new Font(this.getFont().getName(), 0, 10));
        this.addActionListener(this);
    }

    public SmallButton(final Icon icon) {
        this();
        this.setIcon(icon);
    }

    public SmallButton(final String text, final Icon icon) {
        this();
        this.setText(text);
        this.setIcon(icon);
    }

    public SmallButton(final String text) {
        this();
        this.setText(text);
    }
}
