package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.swing.XSpinner;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public abstract class Locator extends JPanel {
    private static final Insets GBC_INSETS = new Insets(0, 0, 0, 0);
    private static final int GBC_ANCHOR = 10;
    private static final int GBC_FILL = 1;

    public Locator() {
        super(new GridBagLayout());
        add(new LABEL1(" "), new GBC(0, 0));
        add(new MEASURE_PANE(), new GBC(1, 0));
        add(new LABEL1(":"), new GBC(2, 0));
        add(new BEAT_PANE(), new GBC(3, 0));
        add(new LABEL1(":"), new GBC(4, 0));
        add(new QUANT_PANE(), new GBC(5, 0));
        add(new LABEL1(":"), new GBC(6, 0));
        add(new TICK_PANE(), new GBC(7, 0));
        add(new LABEL1(" "), new GBC(8, 0));
        add(new LABEL2("Takt " + getContext().getPlayer().getTiming().getBarBeats() + "/" + getContext().getPlayer().getTiming().getBeatUnit()), new GBC(0, 1, 3));
        add(new LABEL2("1/" + getContext().getPlayer().getTiming().getBeatUnit()), new GBC(2, 1, 3));
        add(new LABEL2("1/" + getContext().getPlayer().getTiming().getSubBeatUnit()), new GBC(4, 1, 3));
        add(new LABEL2("1/" + getContext().getPlayer().getTiming().getTickUnit()), new GBC(6, 1, 3));
    }

    protected abstract Context getContext();

    private static class GBC extends GridBagConstraints {
        public GBC(final int x, final int y) {
            this(x, y, 1);
        }

        public GBC(final int x, final int y, final int w) {
            super(x, y, w, 1, 0.0, 0.0, 10, 1, Locator.GBC_INSETS, 0, 0);
        }
    }

    private static class LABEL1 extends JLabel {
        public LABEL1() {
            this("");
        }

        public LABEL1(final String txt) {
            super(txt);
            setFont(new Font(getFont().getName(), 1, 17));
            setHorizontalAlignment(0);
        }
    }

    private static class LABEL2 extends JLabel {
        public LABEL2(final String txt) {
            super(txt);
            setFont(new Font(getFont().getName(), 0, 9));
            setHorizontalAlignment(0);
        }
    }

    private class BEAT_PANE extends LOC_PANE {
        private long m_last;

        private BEAT_PANE() {
            super();
            m_last = 0L;
        }

        protected int getDelta() {
            return getContext().getPlayer().getTiming().getBeatTicks();
        }

        protected void setDisplay(long ticks) {
            ticks /= (long) getDelta();
            ticks %= (long) getContext().getPlayer().getTiming().getBarBeats();
            ++ticks;
            if (m_last != ticks) {
                setText(String.format("%d", ticks));
                m_last = ticks;
            }

        }
    }

    private abstract class LOC_PANE extends XSpinner {

        LOC_PANE() {
            setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            setFont(new Font(getFont().getName(), 1, 17));
            getContext().getPlayer()
                        .addListener(Player.Event.SetPosition, this::onSetPosition);
        }

        protected void decrease(final int exponent) {
            updatePosition(-1, exponent);
        }

        protected abstract int getDelta();

        protected void increase(final int exponent) {
            updatePosition(1, exponent);
        }

        protected void maximize() {
            getContext().getPlayer().setPosition(getContext().getPlayer().getSequence().getTickLength());
        }

        protected void minimize() {
            getContext().getPlayer().setPosition(0L);
        }

        protected abstract void setDisplay(long var1);

        private void updatePosition(int delta, int exponent) {
            while (exponent-- > 0) {
                delta *= 10;
            }

            getContext().getPlayer().setPosition(getContext().getPlayer().getPosition() + ((long) delta * getDelta()));
        }

        public void onSetPosition(final Player player) {
            final long ticks = player.getPosition();
            setDisplay(ticks);
        }
    }

    private class MEASURE_PANE extends LOC_PANE {
        private long m_last;

        private MEASURE_PANE() {
            super();
            m_last = 0L;
        }

        protected int getDelta() {
            return getContext().getPlayer().getTiming().getBarTicks();
        }

        protected void setDisplay(long ticks) {
            ticks /= (long) getDelta();
            ++ticks;
            if (m_last != ticks) {
                setText(String.format("%04d", ticks));
                m_last = ticks;
            }

        }
    }

    private class QUANT_PANE extends LOC_PANE {
        private long m_last;

        private QUANT_PANE() {
            super();
            m_last = 0L;
        }

        protected int getDelta() {
            return getContext().getPlayer().getTiming().getSubBeatTicks();
        }

        protected void setDisplay(long ticks) {
            final int beatQuantization = getContext().getPlayer().getTiming().getSubBeatUnit() / getContext().getPlayer().getTiming().getBeatUnit();
            ticks /= (long) getDelta();
            ticks %= (long) beatQuantization;
            ++ticks;
            if (m_last != ticks) {
                setText(String.format("%d", ticks));
                m_last = ticks;
            }

        }
    }

    private class TICK_PANE extends LOC_PANE {
        private String m_Format;

        private TICK_PANE() {
            super();
            m_Format = null;
        }

        protected int getDelta() {
            return 1;
        }

        protected void setDisplay(final long ticks) {
            if (m_Format == null) {
                final String sQTicks = String.valueOf(getContext().getPlayer().getTiming().getSubBeatTicks());
                m_Format = String.format("%%0%dd", sQTicks.length());
            }

            setText(String.format(m_Format, ticks % (long) getContext().getPlayer().getTiming().getSubBeatTicks()));
        }
    }
}
