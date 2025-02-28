package de.team33.miditor.controller;

import de.team33.midi.Track;
import de.team33.patterns.notes.eris.Channel;

import java.util.function.Consumer;

public interface UIController {

    Track getTrack();

    void setTrack(Track track);

    int[] getTrackSelection();

    void setTrackSelection(int[] selected);

    void addListener(Event event, Consumer<? super UIController> listener);

    enum Event implements Channel<UIController> {
        SetTrack,
        SetTrackSelection,
        UnsetTrack
    }
}
