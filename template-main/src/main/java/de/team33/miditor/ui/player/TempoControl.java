package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.swing.XSpinner;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public abstract class TempoControl {
    private Component m_RootComponent;

    public TempoControl() {
    }

    public Component getComponent() {
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
            getPlayer().addListener(Player.Event.SetTempo, this::onSetTempo);
        }

        protected void decrease(final int exponent) {
            updateTempo(-1, exponent);
        }

        protected void increase(final int exponent) {
            updateTempo(1, exponent);
        }

        protected void maximize() {
            getPlayer().setTempo(180);
        }

        protected void minimize() {
            getPlayer().setTempo(60);
        }

        private void updateTempo(int delta, int exponent) {
            while (exponent-- > 0) {
                delta *= 10;
            }

            getPlayer().setTempo(getPlayer().getTempo() + delta);
        }

        public void onSetTempo(final Player player) {
            final int tempo = player.getTempo();
            setText(String.format("%03d", tempo));
        }
    }
}
