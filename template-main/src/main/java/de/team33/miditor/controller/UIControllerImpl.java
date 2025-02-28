package de.team33.miditor.controller;

import de.team33.messaging.Register;
import de.team33.midi.Track;
import de.team33.patterns.notes.eris.Audience;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

public class UIControllerImpl implements UIController {

    private static final Set<Event> INITIAL_EVENTS = Set.of(Event.SetTrack, Event.SetTrackSelection);

    private final Audience audience = new Audience();
    private int[] m_Selection = new int[0];
    private Track m_Track = null;

    @Override
    public final void addListener(final Event event, final Consumer<? super UIController> listener) {
        audience.add(event, listener);
        if (INITIAL_EVENTS.contains(event)) {
            listener.accept(this);
        }
    }

    public Track getTrack() {
        return this.m_Track;
    }

    public void setTrack(final Track track) {
        audience.send(Event.UnsetTrack, this);
        this.m_Track = track;
        audience.send(Event.SetTrack, this);
    }

    public int[] getTrackSelection() {
        return this.m_Selection;
    }

    public void setTrackSelection(final int[] selected) {
        if (!Arrays.equals(this.m_Selection, selected)) {
            this.m_Selection = selected;
            audience.send(Event.SetTrackSelection, this);
        }
    }
}
