package de.team33.miditor.ui;

import de.team33.miditor.ui.sequence.ActionControl;
import de.team33.miditor.ui.sequence.Context;
import de.team33.miditor.ui.sequence.FileControl;
import de.team33.miditor.ui.sequence.TrackList;

import java.awt.*;

public abstract class SongControls {
    public SongControls() {
    }

    public final Component getActionControl() {
        return new ACTN_CTRL();
    }

    public final Component getFileControl() {
        return new FILE_CTRL();
    }

    protected abstract Context getContext();

    public final Component getTrackList() {
        return new TRCK_LIST();
    }

    private class ACTN_CTRL extends ActionControl {
        private ACTN_CTRL() {
        }

        protected final Context getContext() {
            return SongControls.this.getContext();
        }
    }

    private class FILE_CTRL extends FileControl {
        private FILE_CTRL() {
        }

        protected final Context getContext() {
            return SongControls.this.getContext();
        }
    }

    private class TRCK_LIST extends TrackList {
        private TRCK_LIST() {
        }

        protected final Context getContext() {
            return SongControls.this.getContext();
        }
    }
}
