package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.miditor.ui.Basics;
import de.team33.miditor.ui.XSpinner;

import javax.swing.*;
import java.awt.*;

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;

public abstract class TempoControl {
    private Component m_RootComponent;

    public final Component getComponent() {
        if (m_RootComponent == null) {
            m_RootComponent = label();
        }

        return m_RootComponent;
    }

    protected abstract Player getPlayer();

    private JLabel label() {
        //noinspection MagicNumber
        return XSpinner.builder("%03d")
                       .setBorder(createCompoundBorder(createLineBorder(Color.GRAY),
                                                       createEmptyBorder(1, 4, 1, 4)))
                       .setup(xSpinner -> Basics.setFont(xSpinner, Font.BOLD, 18))
                       .setup(xSpinner -> getPlayer().registry()
                                                     .add(Player.Channel.SET_TEMPO, xSpinner::setValue))
                       .onSetValue(value -> getPlayer().setTempo(value))
                       .build();
    }
}
