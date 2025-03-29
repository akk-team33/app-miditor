package de.team33.miditor.ui.track;

import de.team33.midi.Part;
import de.team33.miditor.controller.UIController;

import javax.swing.*;

public abstract class EventListModel extends AbstractListModel {
    private Part m_Track = null;

    public EventListModel() {
        getTrackHandler().addListener(UIController.Event.SetTrack, this::onSetTrack);
    }

    private void _fireContentsChanged() {
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    public final Object getElementAt(final int index) {
        return m_Track.get(index);
    }

    public final int getSize() {
        return m_Track == null ? 0 : m_Track.size();
    }

    protected abstract UIController getTrackHandler();

    public final void onSetTrack(final UIController controller) {
        final Part track = controller.getTrack();
        if (m_Track != track) {
            m_Track = track;
            if (track == null) {
                _fireContentsChanged();
            } else {
                m_Track.registry().add(Part.Channel.SetEvents, this::onSetEvents);
            }
        }
    }

    private void onSetEvents(final Part track) {
        _fireContentsChanged();
    }
}
