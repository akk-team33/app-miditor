package de.team33.miditor.visible.track;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import net.team33.messaging.Listener;
import net.team33.midi.Track;
import net.team33.miditor.controller.UIController;

public class Header extends JLabel {
    public Header(UIController uiController) {
        this.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        this.setOpaque(true);
        this.setBackground(Color.GRAY);
        this.setForeground(Color.WHITE);
        uiController.getRegister(UIController.SetTrack.class).add(new UICTRL_LSTNR());
    }

    private class TRACK_LSTNR implements Listener<Track.SetName> {
        private TRACK_LSTNR(UIController ctrl) {
            Track track = ctrl.getTrack();
            if (track != null) {
                ctrl.getRegister(UIController.UnsetTrack.class).add(new UNSET_LSTNR());
                track.getRegister(Track.SetName.class).add(this);
            }

        }

        public void pass(Track.SetName message) {
            Header.this.setText(String.format("%s - %s", ((Track)message.getSender()).getPrefix(), ((Track)message.getSender()).getName()));
        }

        private class UNSET_LSTNR implements Listener<UIController.UnsetTrack> {
            private UNSET_LSTNR() {
            }

            public void pass(UIController.UnsetTrack message) {
                UIController ctrl = (UIController)message.getSender();
                Track track = ctrl.getTrack();
                if (track != null) {
                    track.getRegister(Track.SetName.class).remove(TRACK_LSTNR.this);
                    ctrl.getRegister(UIController.UnsetTrack.class).remove(this);
                }

            }
        }
    }

    private class UICTRL_LSTNR implements Listener<UIController.SetTrack> {
        private UICTRL_LSTNR() {
        }

        public void pass(UIController.SetTrack message) {
            Header.this.new TRACK_LSTNR((UIController)message.getSender());
        }
    }
}
