package de.team33.miditor.ui.track;

import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;

import javax.swing.*;

public abstract class EventListModel extends AbstractListModel {
    private Track m_Track = null;

    public EventListModel() {
        getTrackHandler().addListener(UIController.Event.SetTrack, this::onSetTrack);
    }

    private void _fireContentsChanged() {
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    public Object getElementAt(final int index) {
        return m_Track.get(index);
    }

    public int getSize() {
        return m_Track == null ? 0 : m_Track.size();
    }

    protected abstract UIController getTrackHandler();

    public void onSetTrack(final UIController controller) {
        final Track track = controller.getTrack();
        if (m_Track != track) {
            m_Track = track;
            if (track == null) {
                _fireContentsChanged();
            } else {
                m_Track.addListener(Track.Event.SetEvents, this::onSetEvents);
            }
        }
    }

    private void onSetEvents(final Track track) {
        _fireContentsChanged();
    }
}
