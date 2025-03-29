package de.team33.miditor.ui;

import de.team33.midi.Player;
import de.team33.miditor.ui.player.DriveControl;
import de.team33.miditor.ui.player.Locator;
import de.team33.miditor.ui.player.TempoControl;

import java.awt.*;

public abstract class PlayerControls {
    public PlayerControls() {
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

    protected abstract Context getRootContext();

    public interface Context extends de.team33.miditor.ui.player.Context {
    }

    private class DRV_CTRL extends DriveControl {
        private DRV_CTRL() {
        }

        protected final Context getContext() {
            return PlayerControls.this.getRootContext();
        }
    }

    private class LOCATOR extends Locator {
        private LOCATOR() {
        }

        protected final Context getContext() {
            return PlayerControls.this.getRootContext();
        }
    }

    private class TMPO_CTRL extends TempoControl {
        private TMPO_CTRL() {
        }

        protected final Player getPlayer() {
            return PlayerControls.this.getRootContext().getPlayer();
        }
    }
}
