package de.team33.miditor.ui;

import de.team33.midi.Player;
import de.team33.midi.Player.Mode;
import de.team33.midi.Track;
import de.team33.miditor.ui.track.Context;
import de.team33.selection.Selection;
import de.team33.swing.XTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

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
            getContext().getTrack().addListener(Track.Event.SetChannels, this::onSetChannels);
        }

        private void onSetChannels(final Track track) {
            setVisible(false);
            removeAll();
            final int[] channels = track.getChannels();
            if (channels.length == 0) {
                add(TrackControls.this.new LABEL("--"), "Center");
            } else if (channels.length == 1) {
                add(TrackControls.this.new LABEL(String.format("Kanal %02d", channels[0] + 1)), "Center");
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
            getContext().getTrack().addListener(Track.Event.SetModified, this::onSetModified);
            getContext().getSelection().getRegister().add(this::onSelection);
            addActionListener(this::onAction);
        }

        private void onAction(final ActionEvent e) {
            if (isSelected()) {
                getContext().getSelection().add(getContext().getTrack());
            } else {
                getContext().getSelection().remove(getContext().getTrack());
            }
        }

        private void onSetModified(final Track track) {
            setForeground(track.isModified() ? Color.BLUE : Color.BLACK);
        }

        private void onSelection(final Selection.Message<Track> message) {
            setSelected(message.getSender().contains(getContext().getTrack()));
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
            getContext().getPlayer()
                        .addListener(Player.Event.SetModes, this::onSetModes);
        }

        public final void actionPerformed(final ActionEvent e) {
            if (isSelected()) {
                getContext().getPlayer().setMode(getContext().getIndex(), Mode.MUTE);
            } else {
                getContext().getPlayer().setMode(getContext().getIndex(), Mode.NORMAL);
            }

        }

        private void onSetModes(final Player player) {
            final int index = getContext().getIndex();
            final Player.Mode mode = player.getMode(index);
            setSelected(Mode.MUTE == mode);
        }
    }

    private class NAME_PANE extends XTextField {
        public NAME_PANE() {
            super(12);
            getContext().getTrack().addListener(Track.Event.SetName, this::onSetName);
        }

        private void onSetName(final Track track) {
            setText(track.getName());
        }
    }

    private class SOLO_BUTTON extends SmallButton {
        public SOLO_BUTTON() {
            super("solo");
            getContext().getPlayer()
                        .addListener(Player.Event.SetModes, this::onSetModes);
        }

        public final void actionPerformed(final ActionEvent e) {
            if (isSelected()) {
                getContext().getPlayer().setMode(getContext().getIndex(), Mode.SOLO);
            } else {
                getContext().getPlayer().setMode(getContext().getIndex(), Mode.NORMAL);
            }

        }

        private void onSetModes(final Player player) {
            final int index = getContext().getIndex();
            final Player.Mode mode = player.getMode(index);
            setSelected(Mode.SOLO == mode);
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
