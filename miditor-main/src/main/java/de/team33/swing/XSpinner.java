package de.team33.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public abstract class XSpinner extends JLabel {
    private static final long serialVersionUID = -7904911119159118358L;
    private boolean m_Hilited = false;
    private boolean m_Entered = false;
    private boolean m_Focused = false;
    private boolean m_Opaque;
    private Color m_Background;
    private Color m_Foreground;

    public XSpinner() {
        this.setCursor(new Cursor(8));
        this.setFocusable(true);
        this.m_Opaque = this.isOpaque();
        this.m_Background = this.getBackground();
        this.m_Foreground = this.getForeground();
        this.addMouseListener(new MS_CLNT());
        this.addMouseWheelListener(new MSWHEEL_CLNT());
        this.addFocusListener(new FCS_CLNT());
        this.addKeyListener(new KEY_CLNT());
    }

    public final void setOpaque(boolean opaque) {
        this.m_Opaque = opaque;
        if (!this.m_Hilited) {
            super.setOpaque(this.m_Opaque);
        }

    }

    public final void setBackground(Color bg) {
        this.m_Background = bg;
        if (!this.m_Hilited) {
            super.setBackground(this.m_Background);
        }

    }

    public final void setForeground(Color fg) {
        this.m_Foreground = fg;
        if (!this.m_Hilited) {
            super.setForeground(this.m_Foreground);
        }

    }

    protected abstract void increase(int var1);

    protected abstract void decrease(int var1);

    protected abstract void minimize();

    protected abstract void maximize();

    private void setHilited(boolean hilited) {
        if (this.m_Hilited != hilited) {
            if (this.m_Hilited = hilited) {
                super.setOpaque(true);
                super.setForeground(Color.BLACK);
                super.setBackground(Color.LIGHT_GRAY);
            } else {
                super.setOpaque(this.m_Opaque);
                super.setForeground(this.m_Foreground);
                super.setBackground(this.m_Background);
            }
        }

    }

    private void setEntered(boolean entered) {
        this.m_Entered = entered;
        this.setHilited(this.m_Entered || this.m_Focused);
    }

    private void setFocused(boolean focus) {
        this.m_Focused = focus;
        this.setHilited(this.m_Entered || this.m_Focused);
    }

    private class FCS_CLNT implements FocusListener {
        private FCS_CLNT() {
        }

        public final void focusGained(FocusEvent e) {
            XSpinner.this.setFocused(true);
        }

        public final void focusLost(FocusEvent e) {
            XSpinner.this.setFocused(false);
        }
    }

    private class KEY_CLNT extends KeyAdapter {
        private KEY_CLNT() {
        }

        public final void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
            case 33:
                XSpinner.this.increase(e.isShiftDown() ? 2 : 1);
                break;
            case 34:
                XSpinner.this.decrease(e.isShiftDown() ? 2 : 1);
                break;
            case 35:
                XSpinner.this.maximize();
                break;
            case 36:
                XSpinner.this.minimize();
                break;
            case 37:
            case 40:
            case 45:
            case 109:
                XSpinner.this.decrease(e.isShiftDown() ? 1 : 0);
                break;
            case 38:
            case 39:
            case 107:
            case 521:
                XSpinner.this.increase(e.isShiftDown() ? 1 : 0);
            }

        }
    }

    private class MSWHEEL_CLNT implements MouseWheelListener {
        private MSWHEEL_CLNT() {
        }

        public final void mouseWheelMoved(MouseWheelEvent e) {
            if (e.getWheelRotation() < 0) {
                XSpinner.this.increase(e.isShiftDown() ? 1 : 0);
            } else {
                XSpinner.this.decrease(e.isShiftDown() ? 1 : 0);
            }

        }
    }

    private class MS_CLNT extends MouseAdapter {
        private MS_CLNT() {
        }

        public final void mouseEntered(MouseEvent e) {
            XSpinner.this.setEntered(true);
        }

        public final void mouseExited(MouseEvent e) {
            XSpinner.this.setEntered(false);
        }

        public final void mousePressed(MouseEvent e) {
            XSpinner.this.grabFocus();
        }
    }
}
