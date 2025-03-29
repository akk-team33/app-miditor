package de.team33.swing;

import de.team33.swing.prefs.FramePrefs;

import javax.swing.*;
import java.util.prefs.Preferences;

public class XFrame extends JFrame {

    public XFrame(final String ttl, final Preferences prefs, final int closeOperation) {
        super(ttl);
        this.setDefaultCloseOperation(closeOperation);
        (new FramePrefs(prefs)).init(this);
    }

    public XFrame(final String ttl, final Preferences prefs) {
        this(ttl, prefs, 2);
    }
}
