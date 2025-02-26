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
        if (this.m_RootComponent == null) {
            this.m_RootComponent = new LABEL();
        }

        return this.m_RootComponent;
    }

    protected abstract Player getPlayer();

    private class LABEL extends XSpinner {
        private static final long serialVersionUID = -5125403795254334049L;

        public LABEL() {
            this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(1, 4, 1, 4)));
            this.setFont(new Font(this.getFont().getName(), 1, 18));
            TempoControl.this.getPlayer().getRegister(Player.SetTempo.class).add(new PLR_CLNT());
        }

        protected void decrease(int exponent) {
            this.updateTempo(-1, exponent);
        }

        protected void increase(int exponent) {
            this.updateTempo(1, exponent);
        }

        protected void maximize() {
            TempoControl.this.getPlayer().setTempo(180);
        }

        protected void minimize() {
            TempoControl.this.getPlayer().setTempo(60);
        }

        private void updateTempo(int delta, int exponent) {
            while (exponent-- > 0) {
                delta *= 10;
            }

            TempoControl.this.getPlayer().setTempo(TempoControl.this.getPlayer().getTempo() + delta);
        }

        private class PLR_CLNT implements Consumer<Player.SetTempo> {
            private PLR_CLNT() {
            }

            public void accept(Player.SetTempo message) {
                int tempo = message.getSender().getTempo();
                LABEL.this.setText(String.format("%03d", tempo));
            }
        }
    }
}
