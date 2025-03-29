package de.team33.swing.prefs;

import de.team33.swing.setup.FrameSetup;

import java.awt.*;
import java.util.prefs.Preferences;

public abstract class FramePrefs extends FrameSetup {
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_XSTATE = "xstate";

    public FramePrefs() {
    }

    protected abstract Preferences getPreferences();

    protected final int getExtendedState() {
        return this.getPreferences().getInt("xstate", 0);
    }

    protected final void setExtendedState(int value) {
        this.getPreferences().putInt("xstate", value);
    }

    protected final Dimension getSize(int width, int height) {
        return new Dimension(this.getPreferences().getInt("width", width), this.getPreferences().getInt("height", height));
    }

    protected final void setSize(Dimension size) {
        this.getPreferences().putInt("width", size.width);
        this.getPreferences().putInt("height", size.height);
    }
}
