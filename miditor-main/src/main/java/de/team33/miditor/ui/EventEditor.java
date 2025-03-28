package de.team33.miditor.ui;

import de.team33.midi.FullScore;
import de.team33.midi.Part;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.controller.UIControllerImpl;
import de.team33.miditor.ui.event.TableRenderer;
import de.team33.miditor.ui.track.EventActions;
import de.team33.miditor.ui.track.EventTable;
import de.team33.miditor.ui.track.EventTableModel;
import de.team33.miditor.ui.track.Header;
import de.team33.midix.Timing;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public abstract class EventEditor extends UIControllerImpl {
    private static final Insets GBC_INSETS = new Insets(2, 2, 2, 2);
    private static final int GBC_ANCHOR = 10;
    private static final int GBC_FILL = 1;
    private static final GridBagConstraints GBC_HEADER;
    private static final GridBagConstraints GBC_BODY;
    private static final GridBagConstraints GBC_FOOTER;

    static {
        GBC_HEADER = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_BODY = new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_FOOTER = new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
    }

    private final EVNT_RENDERER m_EventRenderer = new EVNT_RENDERER();
    private JComponent m_RootComponent = null;

    protected EventEditor() {
        getSequence().registry().add(FullScore.Channel.SetTracks, this::onSetParts);
    }

    private EventEditor _EventEditor() {
        return this;
    }

    public JComponent getComponent() {
        if (m_RootComponent == null) {
            m_RootComponent = new PANEL();
        }

        return m_RootComponent;
    }

    protected abstract FullScore getSequence();

    private class ACTIONS extends EventActions {
        private ACTIONS() {
        }

        protected UIController getTrackHandler() {
            return _EventEditor();
        }
    }

    private class EVNT_RENDERER extends TableRenderer {
        private EVNT_RENDERER() {
        }

        protected Timing getTiming() {
            return getSequence().getTiming();
        }
    }

    private class EVTBL_MODEL extends EventTableModel {
        private EVTBL_MODEL() {
        }

        protected UIController getTrackHandler() {
            return _EventEditor();
        }
    }

    private class HEADER extends Header {
        public HEADER() {
            super(_EventEditor());
        }
    }

    private class PANEL extends JPanel {
        public PANEL() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createEtchedBorder());
            add(EventEditor.this.new HEADER(), EventEditor.GBC_HEADER);
            add(EventEditor.this.new SCROLL_PANE(), EventEditor.GBC_BODY);
            add((EventEditor.this.new ACTIONS()).getComponent(), EventEditor.GBC_FOOTER);
        }
    }

    private class SCROLL_PANE extends JScrollPane {
        SCROLL_PANE() {
            super(EventEditor.this.new TABLE());
        }
    }

    private void onSetParts(final FullScore sequence) {
        final List<Part> parts = sequence.getTracks();
        final int size = parts.size();
        for (int index = 0; index < size; ++index) {
            final Part track = parts.get(index);
            if (track == getTrack()) {
                return;
            }
        }

        if (parts.isEmpty()) {
            setTrack(null);
        } else {
            setTrack(parts.get(0));
        }
    }

    private class TABLE extends EventTable {
        private TABLE() {
        }

        protected TableRenderer getEventRenderer() {
            return m_EventRenderer;
        }

        protected TableModel getTableModel() {
            return EventEditor.this.new EVTBL_MODEL();
        }

        protected UIController getTrackHandler() {
            return _EventEditor();
        }
    }
}
