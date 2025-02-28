package de.team33.miditor.ui.track;

import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;

import javax.swing.*;
import java.util.function.Consumer;

public abstract class EventListModel extends AbstractListModel {
    private Track m_Track = null;
    private TRCK_CLIENT m_TrckClient = null;

    public EventListModel() {
        getTrackHandler().getRegister(UIController.SetTrack.class)
                         .add(this::onSetTrack);
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

    public void onSetTrack(final UIController.SetTrack message) {
        final Track track = message.getSender().getTrack();
        if (m_Track != track) {
            m_Track = track;
            if (track == null) {
                _fireContentsChanged();
            } else {
                m_TrckClient = new TRCK_CLIENT();
                m_Track.getRegister(Track.SetEvents.class).add(m_TrckClient);
            }
        }
    }

    private class TRCK_CLIENT implements Consumer<Track.SetEvents> {
        private TRCK_CLIENT() {
        }

        public void accept(final Track.SetEvents message) {
            _fireContentsChanged();
        }
    }
}
