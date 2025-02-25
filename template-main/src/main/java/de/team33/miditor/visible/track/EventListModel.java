package de.team33.miditor.visible.track;

import javax.swing.AbstractListModel;
import net.team33.messaging.Listener;
import net.team33.midi.Track;
import net.team33.miditor.controller.UIController;

public abstract class EventListModel extends AbstractListModel {
    private Track m_Track = null;
    private TRCK_CLIENT m_TrckClient = null;

    public EventListModel() {
        this.getTrackHandler().getRegister(UIController.SetTrack.class).add(new EVED_CLIENT());
    }

    private void _fireContentsChanged() {
        this.fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    public Object getElementAt(int index) {
        return this.m_Track.get(index);
    }

    public int getSize() {
        return this.m_Track == null ? 0 : this.m_Track.size();
    }

    protected abstract UIController getTrackHandler();

    private class EVED_CLIENT implements Listener<UIController.SetTrack> {
        private EVED_CLIENT() {
        }

        public void pass(UIController.SetTrack message) {
            Track track = ((UIController)message.getSender()).getTrack();
            if (EventListModel.this.m_Track != track) {
                if (EventListModel.this.m_Track != null) {
                    EventListModel.this.m_Track.getRegister(Track.SetEvents.class).remove(EventListModel.this.m_TrckClient);
                    EventListModel.this.m_TrckClient = null;
                }

                EventListModel.this.m_Track = track;
                if (track == null) {
                    EventListModel.this._fireContentsChanged();
                } else {
                    EventListModel.this.m_TrckClient = EventListModel.this.new TRCK_CLIENT();
                    EventListModel.this.m_Track.getRegister(Track.SetEvents.class).add(EventListModel.this.m_TrckClient);
                }
            }

        }
    }

    private class TRCK_CLIENT implements Listener<Track.SetEvents> {
        private TRCK_CLIENT() {
        }

        public void pass(Track.SetEvents message) {
            EventListModel.this._fireContentsChanged();
        }
    }
}
