package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.swing.XSpinner;
import net.team33.messaging.Listener;

import javax.swing.*;
import java.awt.*;

public abstract class Locator extends JPanel {
    private static final Insets GBC_INSETS = new Insets(0, 0, 0, 0);
    private static final int GBC_ANCHOR = 10;
    private static final int GBC_FILL = 1;

    public Locator() {
        super(new GridBagLayout());
        this.add(new LABEL1(" "), new GBC(0, 0));
        this.add(new MEASURE_PANE(), new GBC(1, 0));
        this.add(new LABEL1(":"), new GBC(2, 0));
        this.add(new BEAT_PANE(), new GBC(3, 0));
        this.add(new LABEL1(":"), new GBC(4, 0));
        this.add(new QUANT_PANE(), new GBC(5, 0));
        this.add(new LABEL1(":"), new GBC(6, 0));
        this.add(new TICK_PANE(), new GBC(7, 0));
        this.add(new LABEL1(" "), new GBC(8, 0));
        this.add(new LABEL2("Takt " + this.getContext().getPlayer().getTiming().getBarBeats() + "/" + this.getContext().getPlayer().getTiming().getBeatUnit()), new GBC(0, 1, 3));
        this.add(new LABEL2("1/" + this.getContext().getPlayer().getTiming().getBeatUnit()), new GBC(2, 1, 3));
        this.add(new LABEL2("1/" + this.getContext().getPlayer().getTiming().getSubBeatUnit()), new GBC(4, 1, 3));
        this.add(new LABEL2("1/" + this.getContext().getPlayer().getTiming().getTickUnit()), new GBC(6, 1, 3));
    }

    protected abstract Context getContext();

    private class BEAT_PANE extends LOC_PANE {
        private long m_last;

        private BEAT_PANE() {
            super();
            this.m_last = 0L;
        }

        protected int getDelta() {
            return Locator.this.getContext().getPlayer().getTiming().getBeatTicks();
        }

        protected void setDisplay(long ticks) {
            ticks /= (long)this.getDelta();
            ticks %= (long)Locator.this.getContext().getPlayer().getTiming().getBarBeats();
            ++ticks;
            if (this.m_last != ticks) {
                this.setText(String.format("%d", ticks));
                this.m_last = ticks;
            }

        }
    }

    private static class GBC extends GridBagConstraints {
        public GBC(int x, int y) {
            this(x, y, 1);
        }

        public GBC(int x, int y, int w) {
            super(x, y, w, 1, 0.0, 0.0, 10, 1, Locator.GBC_INSETS, 0, 0);
        }
    }

    private static class LABEL1 extends JLabel {
        public LABEL1() {
            this("");
        }

        public LABEL1(String txt) {
            super(txt);
            this.setFont(new Font(this.getFont().getName(), 1, 17));
            this.setHorizontalAlignment(0);
        }
    }

    private static class LABEL2 extends JLabel {
        public LABEL2(String txt) {
            super(txt);
            this.setFont(new Font(this.getFont().getName(), 0, 9));
            this.setHorizontalAlignment(0);
        }
    }

    private abstract class LOC_PANE extends XSpinner {
        public LOC_PANE() {
            this.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            this.setFont(new Font(this.getFont().getName(), 1, 17));
            Locator.this.getContext().getPlayer().getRegister(Player.SetPosition.class).add(new PLR_CLNT());
        }

        protected void decrease(int exponent) {
            this.updatePosition(-1, exponent);
        }

        protected abstract int getDelta();

        protected void increase(int exponent) {
            this.updatePosition(1, exponent);
        }

        protected void maximize() {
            Locator.this.getContext().getPlayer().setPosition(Locator.this.getContext().getPlayer().getSequence().getTickLength());
        }

        protected void minimize() {
            Locator.this.getContext().getPlayer().setPosition(0L);
        }

        protected abstract void setDisplay(long var1);

        private void updatePosition(int delta, int exponent) {
            while(exponent-- > 0) {
                delta *= 10;
            }

            Locator.this.getContext().getPlayer().setPosition(Locator.this.getContext().getPlayer().getPosition() + ((long)delta * this.getDelta()));
        }

        private class PLR_CLNT implements Listener<Player.SetPosition> {
            private PLR_CLNT() {
            }

            public void pass(Player.SetPosition message) {
                long ticks = ((Player)message.getSender()).getPosition();
                LOC_PANE.this.setDisplay(ticks);
            }
        }
    }

    private class MEASURE_PANE extends LOC_PANE {
        private long m_last;

        private MEASURE_PANE() {
            super();
            this.m_last = 0L;
        }

        protected int getDelta() {
            return Locator.this.getContext().getPlayer().getTiming().getBarTicks();
        }

        protected void setDisplay(long ticks) {
            ticks /= (long)this.getDelta();
            ++ticks;
            if (this.m_last != ticks) {
                this.setText(String.format("%04d", ticks));
                this.m_last = ticks;
            }

        }
    }

    private class QUANT_PANE extends LOC_PANE {
        private long m_last;

        private QUANT_PANE() {
            super();
            this.m_last = 0L;
        }

        protected int getDelta() {
            return Locator.this.getContext().getPlayer().getTiming().getSubBeatTicks();
        }

        protected void setDisplay(long ticks) {
            int beatQuantization = Locator.this.getContext().getPlayer().getTiming().getSubBeatUnit() / Locator.this.getContext().getPlayer().getTiming().getBeatUnit();
            ticks /= (long)this.getDelta();
            ticks %= (long)beatQuantization;
            ++ticks;
            if (this.m_last != ticks) {
                this.setText(String.format("%d", ticks));
                this.m_last = ticks;
            }

        }
    }

    private class TICK_PANE extends LOC_PANE {
        private String m_Format;

        private TICK_PANE() {
            super();
            this.m_Format = null;
        }

        protected int getDelta() {
            return 1;
        }

        protected void setDisplay(long ticks) {
            if (this.m_Format == null) {
                String sQTicks = String.valueOf(Locator.this.getContext().getPlayer().getTiming().getSubBeatTicks());
                this.m_Format = String.format("%%0%dd", sQTicks.length());
            }

            this.setText(String.format(this.m_Format, ticks % (long)Locator.this.getContext().getPlayer().getTiming().getSubBeatTicks()));
        }
    }
}
