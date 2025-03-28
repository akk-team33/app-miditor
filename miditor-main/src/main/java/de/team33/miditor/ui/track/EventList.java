package de.team33.miditor.ui.track;

import de.team33.miditor.controller.UIController;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public abstract class EventList extends JList {
    public EventList() {
        this.setModel(this.getListModel());
        this.addListSelectionListener(new LS_CLIENT());
    }

    public ListCellRenderer getCellRenderer() {
        return this.getEventRenderer();
    }

    protected abstract UIController getTrackHandler();

    protected abstract ListModel getListModel();

    protected abstract ListCellRenderer getEventRenderer();

    private class LS_CLIENT implements ListSelectionListener {
        private LS_CLIENT() {
        }

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                EventList.this.getTrackHandler().setTrackSelection(EventList.this.getSelectedIndices());
            }

        }
    }
}
