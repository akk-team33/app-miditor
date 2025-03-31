package de.team33.miditor.ui;

import de.team33.miditor.ui.sequence.ActionControl;
import de.team33.miditor.ui.sequence.Context;
import de.team33.miditor.ui.sequence.FileControl;
import de.team33.miditor.ui.sequence.TrackList;

import java.awt.*;

public class SongControls {
    
    private final Context context;

    public SongControls(final Context context) {
        this.context = context;
    }

    public final Component getActionControl() {
        return new ActionControl(context);
    }

    public final Component getFileControl() {
        return new FILE_CTRL();
    }

    public final Component getTrackList() {
        return new TRCK_LIST();
    }

    private class FILE_CTRL extends FileControl {
        private FILE_CTRL() {
        }

        protected final Context getContext() {
            return context;
        }
    }

    private class TRCK_LIST extends TrackList {
        private TRCK_LIST() {
        }

        protected final Context getContext() {
            return context;
        }
    }
}
