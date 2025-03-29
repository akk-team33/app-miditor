package de.team33.swing.prefs;

import de.team33.swing.setup.FrameSetup;

import java.awt.*;
import java.util.prefs.Preferences;

public class FramePrefs extends FrameSetup {

    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_XSTATE = "xstate";

    private final Preferences preferences;

    public FramePrefs(final Preferences preferences) {
        this.preferences = preferences;
    }

    protected final int getExtendedState() {
        return preferences.getInt(KEY_XSTATE, 0);
    }

    protected final void setExtendedState(final int value) {
        preferences.putInt(KEY_XSTATE, value);
    }

    protected final Dimension getSize(final int width, final int height) {
        return new Dimension(preferences.getInt(KEY_WIDTH, width), preferences.getInt(KEY_HEIGHT, height));
    }

    protected final void setSize(final Dimension size) {
        preferences.putInt(KEY_WIDTH, size.width);
        preferences.putInt(KEY_HEIGHT, size.height);
    }
}
