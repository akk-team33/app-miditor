package de.team33.miditor.ui.sequence;

import de.team33.messaging.Listener;
import de.team33.midi.Player;
import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.ui.SmallButton;
import de.team33.miditor.ui.TrackControls;
import de.team33.selection.Selection;
import de.team33.selection.SelectionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

public abstract class TrackList extends JScrollPane {
    public TrackList() {
        this.getViewport().add(new TABLE());
        this.getViewport().setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createEtchedBorder());
    }

    protected abstract Context getContext();

    private abstract class ACTN_BUTTON extends SmallButton {
        private final int min;
        private final int max;

        public ACTN_BUTTON(String lbl, int min) {
            this(lbl, min, Integer.MAX_VALUE);
        }

        public ACTN_BUTTON(String lbl, int min, int max) {
            super(lbl);
            this.min = min;
            this.max = max;
            TrackList.this.getContext().getSelection().getRegister().add(new PRT_SEL_CLNT());
        }

        public void actionPerformed(ActionEvent e) {
            this.doActionWith(TrackList.this.getContext().getSelection());
            this.setSelected(false);
        }

        protected abstract void doActionWith(Iterable<Track> var1);

        private class PRT_SEL_CLNT implements Listener<Selection.Message<Track>> {
            private PRT_SEL_CLNT() {
            }

            public void pass(Selection.Message<Track> message) {
                boolean superMin = ACTN_BUTTON.this.min <= message.getSender().size();
                boolean subMax = message.getSender().size() <= ACTN_BUTTON.this.max;
                ACTN_BUTTON.this.setEnabled(superMin && subMax);
            }
        }
    }

    private class DEL_BUTTON extends ACTN_BUTTON {
        public DEL_BUTTON() {
            super("del", 1);
        }

        protected void doActionWith(Iterable<Track> trcks) {
            TrackList.this.getContext().getSequence().delete(trcks);
        }
    }

    private static class GBC extends GridBagConstraints {
        public GBC(int x, int y) {
            this(x, y, 1);
        }

        public GBC(int x, int y, int w) {
            super(x, y, w, 1, 0.0, 0.0, 10, 2, new Insets(1, 1, 1, 1), 0, 0);
        }
    }

    private class JOIN_BUTTON extends ACTN_BUTTON {
        public JOIN_BUTTON() {
            super("join", 2);
        }

        protected void doActionWith(Iterable<Track> trcks) {
            TrackList.this.getContext().getSequence().join(trcks);
        }
    }

    private class SELECTOR extends JCheckBox {
        SELECTOR() {
            super("alle");
            TrackList.this.getContext().getSelection().getRegister().add(new PRT_SEL_CLNT());
            this.addActionListener(new ACTN_CLNT());
        }

        private class ACTN_CLNT implements ActionListener {
            private ACTN_CLNT() {
            }

            public void actionPerformed(ActionEvent e) {
                if (SELECTOR.this.isSelected()) {
                    SelectionUtil.set(TrackList.this.getContext().getSelection(), TrackList.this.getContext().getSequence().getTracks());
                } else {
                    TrackList.this.getContext().getSelection().clear();
                }

            }
        }

        private class PRT_SEL_CLNT implements Listener<Selection.Message<Track>> {
            private PRT_SEL_CLNT() {
            }

            public void pass(Selection.Message<Track> message) {
                int sel = message.getSender().size();
                SELECTOR.this.setSelected(TrackList.this.getContext().getSequence().getTracks().length - sel < sel);
            }
        }
    }

    private class SEL_ACTIONS extends JPanel {
        SEL_ACTIONS() {
            super(new GridLayout(1, 0, 1, 1));
            this.add(TrackList.this.new JOIN_BUTTON());
            this.add(TrackList.this.new DEL_BUTTON());
        }
    }

    private class SHIFTERS extends TimeShiftControl {
        @Serial
        private static final long serialVersionUID = 3117719685006696735L;

        private SHIFTERS() {
        }

        protected Selection<Track> getSelection() {
            return TrackList.this.getContext().getSelection();
        }

        protected Sequence getSequence() {
            return TrackList.this.getContext().getSequence();
        }
    }

    private class TABLE extends JPanel {
        private final SELECTOR m_Selector = TrackList.this.new SELECTOR();
        private final SEL_ACTIONS m_SelActions = TrackList.this.new SEL_ACTIONS();
        private final Component m_Shifters = TrackList.this.new SHIFTERS();

        public TABLE() {
            super(new GridBagLayout());
            TrackList.this.getContext().getSequence().getRegister(Sequence.SetParts.class).add(new CLIENT());
        }

        private class CLIENT implements Listener<Sequence.SetParts> {
            private CLIENT() {
            }

            public void pass(Sequence.SetParts message) {
                Track[] parts = message.getSender().getTracks();
                TABLE.this.setVisible(false);
                TABLE.this.removeAll();
                int k = parts.length;

                for(int i = 0; i < k; ++i) {
                    TRCK_CTRL details = TrackList.this.new TRCK_CTRL(parts[i], i);
                    TABLE.this.add(details.getIndexPane(), new GBC(0, i));
                    TABLE.this.add(details.getNamePane(), new GBC(1, i));
                    TABLE.this.add(details.getSwitchPane(), new GBC(2, i));
                    TABLE.this.add(details.getChannelPane(), new GBC(3, i));
                }

                TABLE.this.add(new JPanel(), new GBC(0, k++, 4));
                TABLE.this.add(TABLE.this.m_Selector, new GBC(0, k));
                TABLE.this.add(TABLE.this.m_Shifters, new GBC(1, k));
                TABLE.this.add(TABLE.this.m_SelActions, new GBC(2, k));
                TABLE.this.setVisible(true);
            }
        }
    }

    private class TRCK_CONTEXT implements de.team33.miditor.ui.track.Context {
        private final int m_Index;
        private final Track m_Track;

        public TRCK_CONTEXT(Track p, int index) {
            this.m_Index = index;
            this.m_Track = p;
        }

        public int getIndex() {
            return this.m_Index;
        }

        public Player getPlayer() {
            return TrackList.this.getContext().getPlayer();
        }

        public Selection<Track> getSelection() {
            return TrackList.this.getContext().getSelection();
        }

        public Sequence getSequence() {
            return TrackList.this.getContext().getSequence();
        }

        public Track getTrack() {
            return this.m_Track;
        }

        public UIController getTrackHandler() {
            return TrackList.this.getContext().getTrackHandler();
        }
    }

    private class TRCK_CTRL extends TrackControls {
        private final de.team33.miditor.ui.track.Context m_Context;

        public TRCK_CTRL(Track p, int index) {
            this.m_Context = TrackList.this.new TRCK_CONTEXT(p, index);
        }

        protected de.team33.miditor.ui.track.Context getContext() {
            return this.m_Context;
        }
    }
}
