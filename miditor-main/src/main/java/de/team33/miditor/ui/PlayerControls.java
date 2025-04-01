package de.team33.miditor.ui;

import de.team33.midi.Player;
import de.team33.miditor.ui.player.DriveControl;
import de.team33.miditor.ui.player.Locator;
import de.team33.miditor.ui.player.TempoControl;

import javax.swing.*;
import java.awt.*;

public abstract class PlayerControls {

    public PlayerControls() {
    }

    public final JPanel newDriveControl() {
        return DriveControl.by(getRootContext());
    }

    public final JPanel getLocator() {
        return Locator.by(getRootContext());
    }

    public final Component getTempoControl() {
        return (new TMPO_CTRL()).getComponent();
    }

    protected abstract Context getRootContext();

    private class TMPO_CTRL extends TempoControl {
        private TMPO_CTRL() {
        }

        protected final Player getPlayer() {
            return PlayerControls.this.getRootContext().player();
        }
    }
}
