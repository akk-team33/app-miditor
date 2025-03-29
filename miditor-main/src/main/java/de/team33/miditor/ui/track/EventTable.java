package de.team33.miditor.ui.track;

import de.team33.miditor.controller.UIController;
import de.team33.miditor.ui.event.TableRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public abstract class EventTable extends JTable {
    public EventTable() {
        this.setModel(this.getTableModel());
        this.getSelectionModel().addListSelectionListener(new LS_CLIENT());
        this.setAutoResizeMode(0);
    }

    public final TableCellRenderer getCellRenderer(int row, int column) {
        return this.getEventRenderer();
    }

    protected abstract UIController getTrackHandler();

    protected abstract TableModel getTableModel();

    protected abstract TableRenderer getEventRenderer();

    private class LS_CLIENT implements ListSelectionListener {
        private LS_CLIENT() {
        }

        public final void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                EventTable.this.getTrackHandler().setTrackSelection(EventTable.this.getSelectedRows());
            }

        }
    }
}
