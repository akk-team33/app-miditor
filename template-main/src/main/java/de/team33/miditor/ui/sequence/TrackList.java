package de.team33.miditor.ui.sequence;

import de.team33.midi.MidiPlayer;
import de.team33.midi.MidiSequence;
import de.team33.midi.MidiTrack;
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
import java.util.List;
import java.util.Set;

public abstract class TrackList extends JScrollPane {
    public TrackList() {
        getViewport().add(new TABLE());
        getViewport().setBackground(Color.WHITE);
        setBorder(BorderFactory.createEtchedBorder());
    }

    protected abstract Context getContext();

    private static class GBC extends GridBagConstraints {
        public GBC(final int x, final int y) {
            this(x, y, 1);
        }

        public GBC(final int x, final int y, final int w) {
            super(x, y, w, 1, 0.0, 0.0, 10, 2, new Insets(1, 1, 1, 1), 0, 0);
        }
    }

    private abstract class ACTN_BUTTON extends SmallButton {
        private final int min;
        private final int max;

        public ACTN_BUTTON(final String lbl, final int min) {
            this(lbl, min, Integer.MAX_VALUE);
        }

        public ACTN_BUTTON(final String lbl, final int min, final int max) {
            super(lbl);
            this.min = min;
            this.max = max;
            getContext().getSelection().addListener(Selection.Event.UPDATE, this::onUpdate);
        }

        public void actionPerformed(final ActionEvent e) {
            doActionWith(getContext().getSelection());
            setSelected(false);
        }

        protected abstract void doActionWith(Iterable<MidiTrack> var1);

        private void onUpdate(final Set<?> selection) {
            final boolean superMin = min <= selection.size();
            final boolean subMax = selection.size() <= max;
            setEnabled(superMin && subMax);
        }
    }

    private class DEL_BUTTON extends ACTN_BUTTON {
        public DEL_BUTTON() {
            super("del", 1);
        }

        protected void doActionWith(final Iterable<MidiTrack> trcks) {
            getContext().getSequence().delete(trcks);
        }
    }

    private class JOIN_BUTTON extends ACTN_BUTTON {
        public JOIN_BUTTON() {
            super("join", 2);
        }

        protected void doActionWith(final Iterable<MidiTrack> trcks) {
            getContext().getSequence().join(trcks);
        }
    }

    private class SELECTOR extends JCheckBox {
        SELECTOR() {
            super("alle");
            getContext().getSelection().addListener(Selection.Event.UPDATE, this::onUpdate);
            addActionListener(new ACTN_CLNT());
        }

        private class ACTN_CLNT implements ActionListener {
            private ACTN_CLNT() {
            }

            public void actionPerformed(final ActionEvent e) {
                if (isSelected()) {
                    SelectionUtil.set(getContext().getSelection(), getContext().getSequence().getTracks());
                } else {
                    getContext().getSelection().clear();
                }
            }
        }

        private void onUpdate(final Set<?> selection) {
            final int sel = selection.size();
            setSelected((getContext().getSequence().getTracks().size() - sel) < sel);
        }
    }

    private class SEL_ACTIONS extends JPanel {
        SEL_ACTIONS() {
            super(new GridLayout(1, 0, 1, 1));
            add(TrackList.this.new JOIN_BUTTON());
            add(TrackList.this.new DEL_BUTTON());
        }
    }

    private class SHIFTERS extends TimeShiftControl {
        @Serial
        private static final long serialVersionUID = 3117719685006696735L;

        private SHIFTERS() {
        }

        protected Selection<MidiTrack> getSelection() {
            return getContext().getSelection();
        }

        protected MidiSequence getSequence() {
            return getContext().getSequence();
        }
    }

    private class TABLE extends JPanel {
        private final SELECTOR m_Selector = TrackList.this.new SELECTOR();
        private final SEL_ACTIONS m_SelActions = TrackList.this.new SEL_ACTIONS();
        private final Component m_Shifters = TrackList.this.new SHIFTERS();

        TABLE() {
            super(new GridBagLayout());
            getContext().getSequence().add(MidiSequence.Channel.SetTracks, this::onSetParts);
        }

        private void onSetParts(final MidiSequence sequence) {
            final List<MidiTrack> parts = sequence.getTracks();
            setVisible(false);
            removeAll();
            int k = parts.size();

            for (int i = 0; i < k; ++i) {
                final TRCK_CTRL details = TrackList.this.new TRCK_CTRL(parts.get(i), i);
                add(details.getIndexPane(), new GBC(0, i));
                add(details.getNamePane(), new GBC(1, i));
                add(details.getSwitchPane(), new GBC(2, i));
                add(details.getChannelPane(), new GBC(3, i));
            }

            add(new JPanel(), new GBC(0, k++, 4));
            add(m_Selector, new GBC(0, k));
            add(m_Shifters, new GBC(1, k));
            add(m_SelActions, new GBC(2, k));
            setVisible(true);
        }
    }

    private class TRCK_CONTEXT implements de.team33.miditor.ui.track.Context {
        private final int m_Index;
        private final MidiTrack m_Track;

        TRCK_CONTEXT(final MidiTrack p, final int index) {
            m_Index = index;
            m_Track = p;
        }

        public int getIndex() {
            return m_Index;
        }

        public MidiPlayer getPlayer() {
            return getContext().getPlayer();
        }

        public Selection<MidiTrack> getSelection() {
            return getContext().getSelection();
        }

        public MidiSequence getSequence() {
            return getContext().getSequence();
        }

        public MidiTrack getTrack() {
            return m_Track;
        }

        public UIController getTrackHandler() {
            return getContext().getTrackHandler();
        }
    }

    private class TRCK_CTRL extends TrackControls {
        private final de.team33.miditor.ui.track.Context m_Context;

        public TRCK_CTRL(final MidiTrack p, final int index) {
            m_Context = TrackList.this.new TRCK_CONTEXT(p, index);
        }

        protected de.team33.miditor.ui.track.Context getContext() {
            return m_Context;
        }
    }
}
