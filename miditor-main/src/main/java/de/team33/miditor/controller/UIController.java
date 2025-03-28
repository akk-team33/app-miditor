package de.team33.miditor.controller;

import de.team33.midi.Part;
import de.team33.patterns.notes.beta.Channel;

import java.util.function.Consumer;

public interface UIController {

    Part getTrack();

    void setTrack(Part track);

    int[] getTrackSelection();

    void setTrackSelection(int[] selected);

    void addListener(Event event, Consumer<? super UIController> listener);

    enum Event implements Channel<UIController> {
        SetTrack,
        SetTrackSelection,
        UnsetTrack
    }
}
