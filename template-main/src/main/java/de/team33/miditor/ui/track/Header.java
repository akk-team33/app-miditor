package de.team33.miditor.ui.track;

import de.team33.midi.MidiTrack;
import de.team33.miditor.controller.UIController;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class Header extends JLabel {
    public Header(UIController uiController) {
        this.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        this.setOpaque(true);
        this.setBackground(Color.GRAY);
        this.setForeground(Color.WHITE);
        uiController.getRegister(UIController.SetTrack.class).add(new UICTRL_LSTNR());
    }

    private class TRACK_LSTNR implements Consumer<MidiTrack.SetName> {
        private TRACK_LSTNR(UIController ctrl) {
            MidiTrack track = ctrl.getTrack();
            if (track != null) {
                ctrl.getRegister(UIController.UnsetTrack.class).add(new UNSET_LSTNR());
                track.getRegister(MidiTrack.SetName.class).add(this);
            }

        }

        public void accept(MidiTrack.SetName message) {
            Header.this.setText(String.format("%s - %s", ((MidiTrack) message.getSender()).getPrefix(), ((MidiTrack) message.getSender()).getName()));
        }

        private class UNSET_LSTNR implements Consumer<UIController.UnsetTrack> {
            private UNSET_LSTNR() {
            }

            public void accept(UIController.UnsetTrack message) {
                UIController ctrl = (UIController) message.getSender();
                MidiTrack track = ctrl.getTrack();
                if (track != null) {
                    track.getRegister(MidiTrack.SetName.class).remove(TRACK_LSTNR.this);
                    ctrl.getRegister(UIController.UnsetTrack.class).remove(this);
                }

            }
        }
    }

    private class UICTRL_LSTNR implements Consumer<UIController.SetTrack> {
        private UICTRL_LSTNR() {
        }

        public void accept(UIController.SetTrack message) {
            Header.this.new TRACK_LSTNR((UIController) message.getSender());
        }
    }
}
