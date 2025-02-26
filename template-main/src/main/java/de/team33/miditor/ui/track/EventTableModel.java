package de.team33.miditor.ui.track;

import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;
import net.team33.messaging.Listener;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

public abstract class EventTableModel extends AbstractTableModel {
    private static String[] m_ColumnNames = new String[]{"Position", "Kanal", "Typ", "d1", "d2", "Rohdaten"};
    private Track m_Track = null;
    private TRCK_CLIENT m_TrckClient = null;

    public EventTableModel() {
        this.getTrackHandler().getRegister(UIController.SetTrack.class).add(new EVED_CLIENT());
    }

    private void _fireTableChanged() {
        this.fireTableChanged(new TableModelEvent(this));
    }

    public int getColumnCount() {
        return m_ColumnNames.length;
    }

    public String getColumnName(int column) {
        return m_ColumnNames[column];
    }

    public int getRowCount() {
        return this.m_Track == null ? 0 : this.m_Track.size();
    }

    protected abstract UIController getTrackHandler();

    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.m_Track.get(rowIndex);
    }

    private class EVED_CLIENT implements Listener<UIController.SetTrack> {
        private EVED_CLIENT() {
        }

        public void pass(UIController.SetTrack message) {
            Track track = ((UIController)message.getSender()).getTrack();
            if (EventTableModel.this.m_Track != track) {
                if (EventTableModel.this.m_Track != null) {
                    EventTableModel.this.m_Track.getRegister(Track.SetEvents.class).remove(EventTableModel.this.m_TrckClient);
                    EventTableModel.this.m_TrckClient = null;
                }

                EventTableModel.this.m_Track = track;
                if (track == null) {
                    EventTableModel.this._fireTableChanged();
                } else {
                    EventTableModel.this.m_TrckClient = EventTableModel.this.new TRCK_CLIENT();
                    EventTableModel.this.m_Track.getRegister(Track.SetEvents.class).add(EventTableModel.this.m_TrckClient);
                }
            }

        }
    }

    private class TRCK_CLIENT implements Listener<Track.SetEvents> {
        private TRCK_CLIENT() {
        }

        public void pass(Track.SetEvents message) {
            EventTableModel.this._fireTableChanged();
        }
    }
}
