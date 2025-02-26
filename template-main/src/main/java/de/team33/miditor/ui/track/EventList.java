package de.team33.miditor.ui.track;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.team33.miditor.controller.UIController;

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
