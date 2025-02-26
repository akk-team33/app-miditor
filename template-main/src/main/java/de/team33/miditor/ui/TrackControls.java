package de.team33.miditor.ui;

import de.team33.midi.Player;
import de.team33.midi.Player.Mode;
import de.team33.midi.Track;
import de.team33.miditor.ui.track.Context;
import de.team33.selection.Selection;
import net.team33.messaging.Listener;
import net.team33.swing.XTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class TrackControls {
    public TrackControls() {
    }

    public Component getChannelPane() {
        return new CHANNEL_PANE();
    }

    protected abstract Context getContext();

    public Component getIndexPane() {
        return new INDEX_PANE();
    }

    public Component getNamePane() {
        return new NAME_PANE();
    }

    public Component getSwitchPane() {
        return new SWTCH_PANE();
    }

    private class CHANNEL_PANE extends JPanel {
        public CHANNEL_PANE() {
            super(new BorderLayout());
            TrackControls.this.getContext().getTrack().getRegister(Track.SetChannels.class).add(new CLIENT());
        }

        private class CLIENT implements Listener<Track.SetChannels> {
            private CLIENT() {
            }

            public void pass(Track.SetChannels message) {
                CHANNEL_PANE.this.setVisible(false);
                CHANNEL_PANE.this.removeAll();
                int[] channels = ((Track)message.getSender()).getChannels();
                if (channels.length == 0) {
                    CHANNEL_PANE.this.add(TrackControls.this.new LABEL("--"), "Center");
                } else if (channels.length == 1) {
                    CHANNEL_PANE.this.add(TrackControls.this.new LABEL(String.format("Kanal %02d", channels[0] + 1)), "Center");
                } else {
                    CHANNEL_PANE.this.add(TrackControls.this.new SPLIT_BUTTON(), "Center");
                }

                CHANNEL_PANE.this.setVisible(true);
            }
        }
    }

    private class EDIT_BUTTON extends SmallButton {
        public EDIT_BUTTON() {
            super("edit");
        }

        public void actionPerformed(ActionEvent e) {
            TrackControls.this.getContext().getTrackHandler().setTrack(TrackControls.this.getContext().getTrack());
            this.setSelected(false);
        }
    }

    private class INDEX_PANE extends JCheckBox {
        public INDEX_PANE() {
            super(TrackControls.this.getContext().getTrack().getPrefix());
            TrackControls.this.getContext().getTrack().getRegister(Track.SetModified.class).add(new PRT_CLNT());
            TrackControls.this.getContext().getSelection().getRegister().add(new PRT_SEL_CLNT());
            this.addActionListener(new LISTENER());
        }

        private class LISTENER implements ActionListener {
            private LISTENER() {
            }

            public void actionPerformed(ActionEvent e) {
                if (INDEX_PANE.this.isSelected()) {
                    TrackControls.this.getContext().getSelection().add(TrackControls.this.getContext().getTrack());
                } else {
                    TrackControls.this.getContext().getSelection().remove(TrackControls.this.getContext().getTrack());
                }

            }
        }

        private class PRT_CLNT implements Listener<Track.SetModified> {
            private PRT_CLNT() {
            }

            public void pass(Track.SetModified message) {
                INDEX_PANE.this.setForeground(((Track)message.getSender()).isModified() ? Color.BLUE : Color.BLACK);
            }
        }

        private class PRT_SEL_CLNT implements Listener<Selection.Message<Track>> {
            private PRT_SEL_CLNT() {
            }

            public void pass(Selection.Message<Track> message) {
                INDEX_PANE.this.setSelected(((Selection)message.getSender()).contains(TrackControls.this.getContext().getTrack()));
            }
        }
    }

    private class LABEL extends JLabel {
        public LABEL(String txt) {
            super(txt);
            this.setHorizontalAlignment(0);
            this.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }
    }

    private class MUTE_BUTTON extends SmallButton {
        public MUTE_BUTTON() {
            super("mute");
            TrackControls.this.getContext().getPlayer().getRegister(Player.SetModes.class).add(new CLIENT());
        }

        public void actionPerformed(ActionEvent e) {
            if (this.isSelected()) {
                TrackControls.this.getContext().getPlayer().setMode(TrackControls.this.getContext().getIndex(), Mode.MUTE);
            } else {
                TrackControls.this.getContext().getPlayer().setMode(TrackControls.this.getContext().getIndex(), Mode.NORMAL);
            }

        }

        private class CLIENT implements Listener<Player.SetModes> {
            private CLIENT() {
            }

            public void pass(Player.SetModes message) {
                int index = TrackControls.this.getContext().getIndex();
                Player.Mode mode = ((Player)message.getSender()).getMode(index);
                MUTE_BUTTON.this.setSelected(Mode.MUTE == mode);
            }
        }
    }

    private class NAME_PANE extends XTextField {
        public NAME_PANE() {
            super(12);
            TrackControls.this.getContext().getTrack().getRegister(Track.SetName.class).add(new CLIENT());
        }

        private class CLIENT implements Listener<Track.SetName> {
            private CLIENT() {
            }

            public void pass(Track.SetName message) {
                NAME_PANE.this.setText(((Track)message.getSender()).getName());
            }
        }
    }

    private class SOLO_BUTTON extends SmallButton {
        public SOLO_BUTTON() {
            super("solo");
            TrackControls.this.getContext().getPlayer().getRegister(Player.SetModes.class).add(new CLIENT());
        }

        public void actionPerformed(ActionEvent e) {
            if (this.isSelected()) {
                TrackControls.this.getContext().getPlayer().setMode(TrackControls.this.getContext().getIndex(), Mode.SOLO);
            } else {
                TrackControls.this.getContext().getPlayer().setMode(TrackControls.this.getContext().getIndex(), Mode.NORMAL);
            }

        }

        private class CLIENT implements Listener<Player.SetModes> {
            private CLIENT() {
            }

            public void pass(Player.SetModes message) {
                int index = TrackControls.this.getContext().getIndex();
                Player.Mode mode = message.getSender().getMode(index);
                SOLO_BUTTON.this.setSelected(Mode.SOLO == mode);
            }
        }
    }

    private class SPLIT_BUTTON extends SmallButton {
        public SPLIT_BUTTON() {
            super(" split ");
        }

        public void actionPerformed(ActionEvent e) {
            TrackControls.this.getContext().getSequence().split(TrackControls.this.getContext().getTrack());
            this.setSelected(false);
        }
    }

    private class SWTCH_PANE extends JPanel {
        public SWTCH_PANE() {
            super(new GridLayout(1, 0, 1, 1));
            this.add(TrackControls.this.new MUTE_BUTTON());
            this.add(TrackControls.this.new SOLO_BUTTON());
            this.add(TrackControls.this.new EDIT_BUTTON());
        }
    }
}
