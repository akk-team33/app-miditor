package de.team33.swing;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class XTextField extends JTextField {
    public XTextField() {
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(final Document doc, final String text, final int columns) {
        super(doc, text, columns);
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(final int columns) {
        super(columns);
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(final String text, final int columns) {
        super(text, columns);
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(final String text) {
        super(text);
        this.addFocusListener(new SELECTOR());
    }

    private class SELECTOR extends FocusAdapter {
        private SELECTOR() {
        }

        public final void focusGained(final FocusEvent e) {
            ((XTextField) e.getComponent()).selectAll();
        }
    }
}
