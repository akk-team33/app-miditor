package de.team33.miditor.ui.track;

import de.team33.midi.MidiTrack;
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
        uiController.addListener(UIController.Event.SetTrack, this::onSetTrack);
    }

    private void onSetName(final MidiTrack track) {
        setText(String.format("%s - %s", track.getPrefix(), track.name()));
    }

    private void onSetTrack(final UIController controller) {
        Optional.ofNullable(controller.getTrack())
                .ifPresent(track -> track.registry().add(MidiTrack.Channel.SetName, this::onSetName));
    }
}
