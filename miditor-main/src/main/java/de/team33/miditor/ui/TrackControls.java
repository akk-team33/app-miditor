package de.team33.miditor.ui;

import de.team33.midi.Part;
import de.team33.midi.Player;
import de.team33.midi.TrackMode;
import de.team33.miditor.ui.track.Context;
import de.team33.selection.Selection;
import de.team33.swing.XTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.SortedSet;

public abstract class TrackControls {

    public final Component getChannelPane() {
        return new CHANNEL_PANE();
    }

    protected abstract Context getContext();

    public final Component getIndexPane() {
        return new INDEX_PANE();
    }

    public final Component getNamePane() {
        return new NAME_PANE();
    }

    public final Component getSwitchPane() {
        return new SWTCH_PANE();
    }

    private class CHANNEL_PANE extends JPanel {
        CHANNEL_PANE() {
            super(new BorderLayout());
            getContext().getTrack().registry().add(Part.Channel.SetChannels, this::onSetChannels);
        }

        private void onSetChannels(final Part track) {
            setVisible(false);
            removeAll();
            final SortedSet<Integer> channels = track.midiChannels();
            if (channels.isEmpty()) {
                add(TrackControls.this.new LABEL("--"), "Center");
            } else if (channels.size() == 1) {
                add(TrackControls.this.new LABEL(String.format("Kanal %02d", channels.first() + 1)), "Center");
            } else {
                add(TrackControls.this.new SPLIT_BUTTON(), "Center");
            }
            setVisible(true);
        }
    }

    private class EDIT_BUTTON extends SmallButton {
        public EDIT_BUTTON() {
            super("edit");
        }

        public final void actionPerformed(final ActionEvent e) {
            getContext().getTrackHandler().setTrack(getContext().getTrack());
            setSelected(false);
        }
    }

    private class INDEX_PANE extends JCheckBox {
        public INDEX_PANE() {
            super(getContext().getTrack().getPrefix());
            getContext().getTrack().registry().add(Part.Channel.SetModified, this::onSetModified);
            getContext().getSelection().addListener(Selection.Event.UPDATE, this::onSelection);
            addActionListener(this::onAction);
        }

        private void onAction(final ActionEvent e) {
            if (isSelected()) {
                getContext().getSelection().add(getContext().getTrack());
            } else {
                getContext().getSelection().remove(getContext().getTrack());
            }
        }

        private void onSetModified(final Part track) {
            setForeground(track.isModified() ? Color.BLUE : Color.BLACK);
        }

        private void onSelection(final Set<?> selection) {
            setSelected(selection.contains(getContext().getTrack()));
        }
    }

    private class LABEL extends JLabel {
        public LABEL(final String txt) {
            super(txt);
            setHorizontalAlignment(0);
            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }
    }

    private class MUTE_BUTTON extends SmallButton {
        private MUTE_BUTTON() {
            super("mute");
            getContext().getPlayer().registry()
                        .add(Player.Channel.SET_MODES, this::onSetModes);
        }

        public final void actionPerformed(final ActionEvent e) {
            if (isSelected()) {
                getContext().getPlayer().setMode(getContext().getIndex(), TrackMode.MUTE);
            } else {
                getContext().getPlayer().setMode(getContext().getIndex(), TrackMode.NORMAL);
            }

        }

        private void onSetModes(final Player player) {
            final int index = getContext().getIndex();
            final TrackMode mode = player.getMode(index);
            setSelected(TrackMode.MUTE == mode);
        }
    }

    private class NAME_PANE extends XTextField {
        public NAME_PANE() {
            super(12);
            getContext().getTrack().registry().add(Part.Channel.SetName, this::onSetName);
        }

        private void onSetName(final Part track) {
            setText(track.name());
        }
    }

    private class SOLO_BUTTON extends SmallButton {
        public SOLO_BUTTON() {
            super("solo");
            getContext().getPlayer().registry()
                        .add(Player.Channel.SET_MODES, this::onSetModes);
        }

        public final void actionPerformed(final ActionEvent e) {
            if (isSelected()) {
                getContext().getPlayer().setMode(getContext().getIndex(), TrackMode.SOLO);
            } else {
                getContext().getPlayer().setMode(getContext().getIndex(), TrackMode.NORMAL);
            }

        }

        private void onSetModes(final Player player) {
            final int index = getContext().getIndex();
            final TrackMode mode = player.getMode(index);
            setSelected(TrackMode.SOLO == mode);
        }
    }

    private class SPLIT_BUTTON extends SmallButton {
        SPLIT_BUTTON() {
            super(" split ");
        }

        public final void actionPerformed(final ActionEvent e) {
            getContext().getSequence().split(getContext().getTrack());
            setSelected(false);
        }
    }

    private class SWTCH_PANE extends JPanel {
        SWTCH_PANE() {
            super(new GridLayout(1, 0, 1, 1));
            add(TrackControls.this.new MUTE_BUTTON());
            add(TrackControls.this.new SOLO_BUTTON());
            add(TrackControls.this.new EDIT_BUTTON());
        }
    }
}
