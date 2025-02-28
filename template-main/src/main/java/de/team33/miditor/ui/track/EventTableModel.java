package de.team33.miditor.ui.track;

import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.function.Consumer;

public abstract class EventTableModel extends AbstractTableModel {
    private static String[] m_ColumnNames = new String[]{"Position", "Kanal", "Typ", "d1", "d2", "Rohdaten"};
    private Track m_Track = null;
    private TRCK_CLIENT m_TrckClient = null;

    public EventTableModel() {
        getTrackHandler().getRegister(UIController.SetTrack.class)
                         .add(this::onSetTrack);
    }

    private void _fireTableChanged() {
        fireTableChanged(new TableModelEvent(this));
    }

    public int getColumnCount() {
        return m_ColumnNames.length;
    }

    public String getColumnName(final int column) {
        return m_ColumnNames[column];
    }

    public int getRowCount() {
        return m_Track == null ? 0 : m_Track.size();
    }

    protected abstract UIController getTrackHandler();

    public Object getValueAt(final int rowIndex, final int columnIndex) {
        return m_Track.get(rowIndex);
    }

    private void onSetTrack(final UIController.SetTrack message) {
        final Track track = message.getSender().getTrack();
        if (m_Track != track) {
            m_Track = track;
            if (null == track) {
                _fireTableChanged();
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
            _fireTableChanged();
        }
    }
}
