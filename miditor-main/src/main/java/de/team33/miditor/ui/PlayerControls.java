package de.team33.miditor.ui;

import de.team33.midi.Player;
import de.team33.miditor.ui.player.DriveControl;
import de.team33.miditor.ui.player.Locator;
import de.team33.miditor.ui.player.TempoControl;

import java.awt.*;

public class PlayerControls {

    private final Context context;

    public PlayerControls(final Context context) {
        this.context = context;
    }

    public final Component getDriveControl() {
        return new DRV_CTRL();
    }

    public final Component getLocator() {
        return new LOCATOR();
    }

    public final Component getTempoControl() {
        return (new TMPO_CTRL()).getComponent();
    }

    private class DRV_CTRL extends DriveControl {
        private DRV_CTRL() {
        }

        protected final Context getContext() {
            return context;
        }
    }

    private class LOCATOR extends Locator {
        private LOCATOR() {
        }

        protected final Context getContext() {
            return context;
        }
    }

    private class TMPO_CTRL extends TempoControl {
        private TMPO_CTRL() {
        }

        protected final Player getPlayer() {
            return context.player();
        }
    }
}
