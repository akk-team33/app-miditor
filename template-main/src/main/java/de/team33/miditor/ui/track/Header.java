package de.team33.miditor.ui.track;

import de.team33.midi.Track;
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

    private class TRACK_LSTNR implements Consumer<Track.SetName> {
        private TRACK_LSTNR(UIController ctrl) {
            Track track = ctrl.getTrack();
            if (track != null) {
                track.getRegister(Track.SetName.class).add(this);
            }
        }

        public void accept(Track.SetName message) {
            Header.this.setText(String.format("%s - %s", ((Track) message.getSender()).getPrefix(), ((Track) message.getSender()).getName()));
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
