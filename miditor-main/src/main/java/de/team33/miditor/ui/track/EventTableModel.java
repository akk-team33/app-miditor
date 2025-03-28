package de.team33.miditor.ui.track;

import de.team33.midi.Part;
import de.team33.miditor.controller.UIController;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

public abstract class EventTableModel extends AbstractTableModel {
    private static String[] m_ColumnNames = {"Position", "Kanal", "Typ", "d1", "d2", "Rohdaten"};

    private Part m_Track = null;

    protected EventTableModel() {
        getTrackHandler().addListener(UIController.Event.SetTrack, this::onSetTrack);
    }

    private void _fireTableChanged() {
        fireTableChanged(new TableModelEvent(this));
    }

    public final int getColumnCount() {
        return m_ColumnNames.length;
    }

    public final String getColumnName(final int column) {
        return m_ColumnNames[column];
    }

    public final int getRowCount() {
        return m_Track == null ? 0 : m_Track.size();
    }

    protected abstract UIController getTrackHandler();

    public final Object getValueAt(final int rowIndex, final int columnIndex) {
        return m_Track.get(rowIndex);
    }

    private void onSetTrack(final UIController controller) {
        final Part track = controller.getTrack();
        if (m_Track != track) {
            m_Track = track;
            if (null == track) {
                _fireTableChanged();
            } else {
                m_Track.registry().add(Part.Channel.SetEvents, this::onSetEvents);
            }
        }
    }

    private void onSetEvents(final Part track) {
        _fireTableChanged();
    }
}
