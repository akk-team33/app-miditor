package de.team33.miditor.controller;

import de.team33.midi.MidiTrack;
import de.team33.patterns.notes.alpha.Channel;

import java.util.function.Consumer;

public interface UIController {

    MidiTrack getTrack();

    void setTrack(MidiTrack track);

    int[] getTrackSelection();

    void setTrackSelection(int[] selected);

    void addListener(Event event, Consumer<? super UIController> listener);

    enum Event implements Channel<UIController> {
        SetTrack,
        SetTrackSelection,
        UnsetTrack
    }
}
