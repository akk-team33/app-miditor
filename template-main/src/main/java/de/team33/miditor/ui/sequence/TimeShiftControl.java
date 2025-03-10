package de.team33.miditor.ui.sequence;

import de.team33.midi.Sequence;
import de.team33.midi.MidiTrack;
import de.team33.midix.Timing;
import de.team33.miditor.model.TimeShift;
import de.team33.miditor.model.TimeShiftBase;
import de.team33.selection.Selection;
import de.team33.swing.XButton;
import de.team33.swing.XSpinner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;

public abstract class TimeShiftControl extends JPanel {
    private final TimeShift m_TimeShift = new TIMESHIFT();

    public TimeShiftControl() {
        add(new SHFT_LEFT_BTTN());
        add(new DIVIDEND_INPUT());
        add(new JLabel("/"));
        add(new DIVISOR_INPUT());
        add(new SHFT_RGHT_BTTN());
    }

    protected abstract Sequence getSequence();

    protected abstract Selection<MidiTrack> getSelection();

    public class DIVIDEND_INPUT extends XSpinner {
        DIVIDEND_INPUT() {
            m_TimeShift.addListener(TimeShift.Event.SetDividend, this::onSetDividend);
        }

        protected void decrease(final int exponent) {
            updateDividend(-1, exponent);
        }

        protected void increase(final int exponent) {
            updateDividend(1, exponent);
        }

        protected void maximize() {
            m_TimeShift.setDividend(Integer.MAX_VALUE);
        }

        protected void minimize() {
            m_TimeShift.setDividend(1);
        }

        private void updateDividend(int delta, int exponent) {
            while (exponent-- > 0) {
                delta *= 10;
            }

            int newDividend = m_TimeShift.getDividend() + delta;
            if (newDividend < 1) {
                newDividend = 1;
            }

            m_TimeShift.setDividend(newDividend);
        }

        private void onSetDividend(final TimeShift timeShift) {
            final int value = timeShift.getDividend();
            setText(String.valueOf(value));
        }
    }

    public class DIVISOR_INPUT extends XSpinner {
        DIVISOR_INPUT() {
            m_TimeShift.addListener(TimeShift.Event.SetDivisor, this::onSetDivisor);
        }

        protected void decrease(final int exponent) {
            m_TimeShift.setDivisor(m_TimeShift.getPrevDivisor());
        }

        protected void increase(final int exponent) {
            m_TimeShift.setDivisor(m_TimeShift.getNextDivisor());
        }

        protected void maximize() {
            m_TimeShift.setDivisor(m_TimeShift.getTickUnit());
        }

        protected void minimize() {
            m_TimeShift.setDivisor(1);
        }

        private void onSetDivisor(final TimeShift timeShift) {
            final int value = timeShift.getDivisor();
            setText(String.valueOf(value));
        }
    }

    private class SHFT_LEFT_BTTN extends SHIFT_BTTN {
        @Serial
        private static final long serialVersionUID = -3179519949089028935L;

        SHFT_LEFT_BTTN() {
            super("<<", -1);
        }
    }

    private class SHFT_RGHT_BTTN extends SHIFT_BTTN {
        @Serial
        private static final long serialVersionUID = -6754419310381932720L;

        SHFT_RGHT_BTTN() {
            super(">>", 1);
        }
    }

    private class SHIFT_BTTN extends XButton {
        @Serial
        private static final long serialVersionUID = -7471764314927669434L;
        private final int m_Factor;

        SHIFT_BTTN(final String text, final int factor) {
            super(text);
            m_Factor = factor;
            setMargin(new Insets(0, 0, 0, 0));
            setFont(new Font(getFont().getName(), 0, 10));
            getSelection().addListener(Selection.Event.UPDATE, this::onUpdate);
        }

        public void actionPerformed(final ActionEvent e) {
            for (final MidiTrack p : getSelection()) {
                p.shift(((long) m_TimeShift.getTicks() * m_Factor));
            }
        }

        private void onUpdate(final Selection<?> selection) {
            setEnabled(1 <= selection.size());
        }
    }

    private class TIMESHIFT extends TimeShiftBase {
        protected Timing getTiming() {
            return getSequence().getTiming();
        }
    }
}
