package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.swing.XSpinner;

import javax.swing.*;
import java.awt.*;

public abstract class TempoControl {
    private Component m_RootComponent;

    public final Component getComponent() {
        if (m_RootComponent == null) {
            m_RootComponent = new LABEL();
        }

        return m_RootComponent;
    }

    protected abstract Player getPlayer();

    private class LABEL extends XSpinner {
        private static final long serialVersionUID = -5125403795254334049L;

        public LABEL() {
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(1, 4, 1, 4)));
            setFont(new Font(getFont().getName(), 1, 18));
            getPlayer().registry().add(Player.Channel.SET_TEMPO, this::onSetTempo);
        }

        protected final void decrease(final int exponent) {
            updateTempo(-1, exponent);
        }

        protected final void increase(final int exponent) {
            updateTempo(1, exponent);
        }

        protected final void maximize() {
            getPlayer().setTempo(180);
        }

        protected final void minimize() {
            getPlayer().setTempo(60);
        }

        private void updateTempo(int delta, int exponent) {
            while (exponent-- > 0) {
                delta *= 10;
            }

            getPlayer().setTempo(getPlayer().getTempo() + delta);
        }

        public final void onSetTempo(final int tempo) {
            setText(String.format("%03d", tempo));
        }
    }
}
