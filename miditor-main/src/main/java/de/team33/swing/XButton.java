package de.team33.swing;

import javax.swing.*;
import java.awt.event.ActionListener;

public abstract class XButton extends JButton implements ActionListener {
    public XButton(final Icon ico) {
        super(ico);
        this.addActionListener(this);
    }

    public XButton(final String text) {
        super(text);
        this.addActionListener(this);
    }
}
