package de.team33.miditor.controller;

import de.team33.messaging.Registry;
import de.team33.midi.MidiTrack;

public interface UIController extends Registry<UIController.Message> {
    MidiTrack getTrack();

    void setTrack(MidiTrack var1);

    int[] getTrackSelection();

    void setTrackSelection(int[] var1);

    public interface Message extends de.team33.messaging.Message<UIController> {
    }

    public interface SetTrack extends Message {
    }

    public interface SetTrackSelection extends Message {
    }

    public interface UnsetTrack extends Message {
    }
}
