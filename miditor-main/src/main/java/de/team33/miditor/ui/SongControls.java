package de.team33.miditor.ui;

import de.team33.miditor.ui.sequence.Context;
import de.team33.miditor.ui.sequence.FileControl;
import de.team33.miditor.ui.sequence.TrackList;

import java.awt.*;

public abstract class SongControls {

    public final Component getFileControl() {
        return new FILE_CTRL();
    }

    protected abstract Context getContext();

    public final Component getTrackList() {
        return new TRCK_LIST();
    }

    private class FILE_CTRL extends FileControl {

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
