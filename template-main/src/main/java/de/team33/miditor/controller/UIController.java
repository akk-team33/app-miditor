package de.team33.miditor.controller;

import de.team33.midi.Track;
import net.team33.messaging.Registry;

public interface UIController extends Registry<UIController.Message> {
    Track getTrack();

    int[] getTrackSelection();

    void setTrack(Track var1);

    void setTrackSelection(int[] var1);

    public interface Message extends net.team33.messaging.Message<UIController> {
    }

    public interface SetTrack extends Message {
    }

    public interface SetTrackSelection extends Message {
    }

    public interface UnsetTrack extends Message {
    }
}
