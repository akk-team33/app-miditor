package de.team33.swing;

import de.team33.swing.prefs.FramePrefs;

import javax.swing.*;
import java.util.prefs.Preferences;

public class XFrame extends JFrame {
    public XFrame(final String ttl, final Preferences prefs, final int closeOperation) {
        super(ttl);
        this.setDefaultCloseOperation(closeOperation);
        (new PREFS(prefs)).init(this);
    }

    public XFrame(final String ttl, final Preferences prefs) {
        this(ttl, prefs, 2);
    }

    private static class PREFS extends FramePrefs {
        private Preferences prefs;

        private PREFS(final Preferences prefs) {
            this.prefs = prefs;
        }

        protected final Preferences getPreferences() {
            return this.prefs;
        }
    }
}
