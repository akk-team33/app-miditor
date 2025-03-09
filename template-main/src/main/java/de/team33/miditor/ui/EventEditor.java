package de.team33.miditor.ui;

import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.midix.Timing;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.controller.UIControllerImpl;
import de.team33.miditor.ui.event.TableRenderer;
import de.team33.miditor.ui.track.EventActions;
import de.team33.miditor.ui.track.EventTable;
import de.team33.miditor.ui.track.EventTableModel;
import de.team33.miditor.ui.track.Header;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.function.Consumer;

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

    public EventEditor() {
        this.getSequence().getRegister(Sequence.SetParts.class).add(new SONG_CLIENT());
    }

    private EventEditor _EventEditor() {
        return this;
    }

    public JComponent getComponent() {
        if (this.m_RootComponent == null) {
            this.m_RootComponent = new PANEL();
        }

        return this.m_RootComponent;
    }

    protected abstract Sequence getSequence();

    private class ACTIONS extends EventActions {
        private ACTIONS() {
        }

        protected UIController getTrackHandler() {
            return EventEditor.this._EventEditor();
        }
    }

    private class EVNT_RENDERER extends TableRenderer {
        private EVNT_RENDERER() {
        }

        protected Timing getTiming() {
            return EventEditor.this.getSequence().getTiming();
        }
    }

    private class EVTBL_MODEL extends EventTableModel {
        private EVTBL_MODEL() {
        }

        protected UIController getTrackHandler() {
            return EventEditor.this._EventEditor();
        }
    }

    private class HEADER extends Header {
        public HEADER() {
            super(EventEditor.this._EventEditor());
        }
    }

    private class PANEL extends JPanel {
        public PANEL() {
            super(new GridBagLayout());
            this.setBorder(BorderFactory.createEtchedBorder());
            this.add(EventEditor.this.new HEADER(), EventEditor.GBC_HEADER);
            this.add(EventEditor.this.new SCROLL_PANE(), EventEditor.GBC_BODY);
            this.add((EventEditor.this.new ACTIONS()).getComponent(), EventEditor.GBC_FOOTER);
        }
    }

    private class SCROLL_PANE extends JScrollPane {
        SCROLL_PANE() {
            super(EventEditor.this.new TABLE());
        }
    }

    private class SONG_CLIENT implements Consumer<Sequence.SetParts> {
        private SONG_CLIENT() {
        }

        public void accept(Sequence.SetParts message) {
            Track[] parts = ((Sequence) message.getSender()).getTracks();
            Track[] var6 = parts;
            int var5 = parts.length;

            for (int var4 = 0; var4 < var5; ++var4) {
                Track track = var6[var4];
                if (track == EventEditor.this.getTrack()) {
                    return;
                }
            }

            if (parts.length == 0) {
                EventEditor.this.setTrack((Track) null);
            } else {
                EventEditor.this.setTrack(parts[0]);
            }

        }
    }

    private class TABLE extends EventTable {
        private TABLE() {
        }

        protected TableRenderer getEventRenderer() {
            return EventEditor.this.m_EventRenderer;
        }

        protected TableModel getTableModel() {
            return EventEditor.this.new EVTBL_MODEL();
        }

        protected UIController getTrackHandler() {
            return EventEditor.this._EventEditor();
        }
    }
}
