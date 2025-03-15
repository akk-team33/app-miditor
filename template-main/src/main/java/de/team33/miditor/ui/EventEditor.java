package de.team33.miditor.ui;

import de.team33.midi.MidiSequence;
import de.team33.midi.MidiTrack;
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
        getSequence().add(MidiSequence.Channel.SetTracks, this::onSetParts);
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

    protected abstract MidiSequence getSequence();

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

    private void onSetParts(final MidiSequence sequence) {
        final MidiTrack[] parts = sequence.getTracks();
        final MidiTrack[] var6 = parts;
        final int var5 = parts.length;

        for (int var4 = 0; var4 < var5; ++var4) {
            final MidiTrack track = var6[var4];
            if (track == getTrack()) {
                return;
            }
        }

        if (parts.length == 0) {
            setTrack((MidiTrack) null);
        } else {
            setTrack(parts[0]);
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
