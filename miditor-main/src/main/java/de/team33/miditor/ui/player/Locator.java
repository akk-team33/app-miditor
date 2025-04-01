package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.midi.Timing;
import de.team33.miditor.ui.Basics;
import de.team33.miditor.ui.Context;
import de.team33.sphinx.alpha.visual.JLabels;
import de.team33.swing.XSpinner;

import javax.swing.*;
import java.awt.*;

public final class Locator extends JPanel {

    private static final Insets GBC_INSETS = new Insets(0, 0, 0, 0);
    private static final int GBC_ANCHOR = GridBagConstraints.CENTER;
    private static final int GBC_FILL = GridBagConstraints.NONE;

    private final Context context;

    private Locator(final Context context) {
        super(new GridBagLayout());
        this.context = context;
        final Timing timing = context.timing();
        add(largeLabel(" "), gbc(0, 0));
        add(new MEASURE_PANE(), gbc(1, 0));
        add(largeLabel(":"), gbc(2, 0));
        add(new BEAT_PANE(), gbc(3, 0));
        add(largeLabel(":"), gbc(4, 0));
        add(new QUANT_PANE(), gbc(5, 0));
        add(largeLabel(":"), gbc(6, 0));
        add(new TICK_PANE(), gbc(7, 0));
        add(largeLabel(" "), gbc(8, 0));
        add(smallLabel("Takt " + timing.barNumerator() + "/" + timing.barDenominator()), gbc(0, 1, 3));
        add(smallLabel("1/" + timing.barDenominator()), gbc(2, 1, 3));
        add(smallLabel("1/" + timing.subBeatDenominator()), gbc(4, 1, 3));
        add(smallLabel("1/" + timing.tickDenominator()), gbc(6, 1, 3));
    }

    public static JPanel by(final Context context) {
        return new Locator(context);
    }

    private static GridBagConstraints gbc(final int x, final int y) {
        return gbc(x, y, 1);
    }

    private static GridBagConstraints gbc(final int x, final int y, final int w) {
        return new GridBagConstraints(x, y, w, 1, 0.0, 0.0, GBC_ANCHOR, GBC_FILL, GBC_INSETS, 0, 0);
    }

    private static JLabel largeLabel(final String text) {
        return JLabels.builder()
                      .setText(text)
                      .setup(label -> Basics.setFont(label, Font.BOLD, 17))
                      .setHorizontalAlignment(SwingConstants.CENTER)
                      .build();
    }

    private static JLabel smallLabel(final String text) {
        return JLabels.builder()
                      .setText(text)
                      .setup(label -> Basics.setFont(label, Font.PLAIN, 9))
                      .setHorizontalAlignment(SwingConstants.CENTER)
                      .build();
    }

    private class BEAT_PANE extends LOC_PANE {
        private long m_last;

        private BEAT_PANE() {
            super();
            m_last = 0L;
        }

        protected final int getDelta() {
            return context.timing().beatTicks();
        }

        protected final void setDisplay(long ticks) {
            ticks /= (long) getDelta();
            ticks %= (long) context.timing().barNumerator();
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
            context.player().registry()
                        .add(Player.Channel.SET_POSITION, this::onSetPosition);
        }

        protected final void decrease(final int exponent) {
            updatePosition(-1, exponent);
        }

        protected abstract int getDelta();

        protected final void increase(final int exponent) {
            updatePosition(1, exponent);
        }

        protected final void maximize() {
            context.player().setPosition(context.score().getTickLength());
        }

        protected final void minimize() {
            context.player().setPosition(0L);
        }

        protected abstract void setDisplay(long var1);

        private void updatePosition(int delta, int exponent) {
            while (exponent-- > 0) {
                delta *= 10;
            }

            context.player().setPosition(context.player().getPosition() + ((long) delta * getDelta()));
        }

        public final void onSetPosition(final long ticks) {
            setDisplay(ticks);
        }
    }

    private class MEASURE_PANE extends LOC_PANE {
        private long m_last;

        private MEASURE_PANE() {
            super();
            m_last = 0L;
        }

        protected final int getDelta() {
            return context.timing().barTicks();
        }

        protected final void setDisplay(long ticks) {
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

        protected final int getDelta() {
            return context.timing().subBeatTicks();
        }

        protected final void setDisplay(long ticks) {
            final int beatQuantization = context.timing().subBeatDenominator() / context.timing().barDenominator();
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

        protected final int getDelta() {
            return 1;
        }

        protected final void setDisplay(final long ticks) {
            if (m_Format == null) {
                final String sQTicks = String.valueOf(context.timing().subBeatTicks());
                m_Format = String.format("%%0%dd", sQTicks.length());
            }

            setText(String.format(m_Format, ticks % (long) context.timing().subBeatTicks()));
        }
    }
}
