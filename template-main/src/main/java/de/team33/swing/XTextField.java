package de.team33.swing;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class XTextField extends JTextField {
    public XTextField() {
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(int columns) {
        super(columns);
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(String text, int columns) {
        super(text, columns);
        this.addFocusListener(new SELECTOR());
    }

    public XTextField(String text) {
        super(text);
        this.addFocusListener(new SELECTOR());
    }

    private class SELECTOR extends FocusAdapter {
        private SELECTOR() {
        }

        public void focusGained(FocusEvent e) {
            ((XTextField) e.getComponent()).selectAll();
        }
    }
}
