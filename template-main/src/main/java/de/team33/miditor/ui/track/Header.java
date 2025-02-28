package de.team33.miditor.ui.track;

import de.team33.midi.Track;
import de.team33.miditor.controller.UIController;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class Header extends JLabel {
    public Header(final UIController uiController) {
        setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        setOpaque(true);
        setBackground(Color.GRAY);
        setForeground(Color.WHITE);
        uiController.getRegister(UIController.SetTrack.class)
                    .add(this::onSetTrack);
    }

    private void onSetName(final Track track) {
        setText(String.format("%s - %s", track.getPrefix(), track.getName()));
    }

    private void onSetTrack(final UIController.SetTrack message) {
        Optional.ofNullable(message.getSender().getTrack())
                .ifPresent(track -> track.addListener(Track.Event.SetName, this::onSetName));
    }
}
