package de.team33.miditor.ui.sequence;

import de.team33.midi.Sequence;
import de.team33.midi.Timing;
import de.team33.midi.Track;
import de.team33.miditor.model.TimeShift;
import de.team33.miditor.model.TimeShiftBase;
import de.team33.selection.Selection;
import de.team33.swing.XButton;
import de.team33.swing.XSpinner;
import net.team33.messaging.Listener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;

public abstract class TimeShiftControl extends JPanel {
    private final TimeShift m_TimeShift = new TIMESHIFT();

    public TimeShiftControl() {
        this.add(new SHFT_LEFT_BTTN());
        this.add(new DIVIDEND_INPUT());
        this.add(new JLabel("/"));
        this.add(new DIVISOR_INPUT());
        this.add(new SHFT_RGHT_BTTN());
    }

    protected abstract Sequence getSequence();

    protected abstract Selection<Track> getSelection();

    public class DIVIDEND_INPUT extends XSpinner {
        DIVIDEND_INPUT() {
            TimeShiftControl.this.m_TimeShift.getRegister(TimeShift.SetDividend.class).add(new TS_CLIENT());
        }

        protected void decrease(int exponent) {
            this.updateDividend(-1, exponent);
        }

        protected void increase(int exponent) {
            this.updateDividend(1, exponent);
        }

        protected void maximize() {
            TimeShiftControl.this.m_TimeShift.setDividend(Integer.MAX_VALUE);
        }

        protected void minimize() {
            TimeShiftControl.this.m_TimeShift.setDividend(1);
        }

        private void updateDividend(int delta, int exponent) {
            while(exponent-- > 0) {
                delta *= 10;
            }

            int newDividend = TimeShiftControl.this.m_TimeShift.getDividend() + delta;
            if (newDividend < 1) {
                newDividend = 1;
            }

            TimeShiftControl.this.m_TimeShift.setDividend(newDividend);
        }

        private class TS_CLIENT implements Listener<TimeShift.SetDividend> {
            private TS_CLIENT() {
            }

            public void pass(TimeShift.SetDividend message) {
                int value = message.getSender().getDividend();
                DIVIDEND_INPUT.this.setText(String.valueOf(value));
            }
        }
    }

    public class DIVISOR_INPUT extends XSpinner {
        DIVISOR_INPUT() {
            TimeShiftControl.this.m_TimeShift.getRegister(TimeShift.SetDivisor.class).add(new TS_CLIENT());
        }

        protected void decrease(int exponent) {
            TimeShiftControl.this.m_TimeShift.setDivisor(TimeShiftControl.this.m_TimeShift.getPrevDivisor());
        }

        protected void increase(int exponent) {
            TimeShiftControl.this.m_TimeShift.setDivisor(TimeShiftControl.this.m_TimeShift.getNextDivisor());
        }

        protected void maximize() {
            TimeShiftControl.this.m_TimeShift.setDivisor(TimeShiftControl.this.m_TimeShift.getTickUnit());
        }

        protected void minimize() {
            TimeShiftControl.this.m_TimeShift.setDivisor(1);
        }

        private class TS_CLIENT implements Listener<TimeShift.SetDivisor> {
            private TS_CLIENT() {
            }

            public void pass(TimeShift.SetDivisor message) {
                int value = message.getSender().getDivisor();
                DIVISOR_INPUT.this.setText(String.valueOf(value));
            }
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

        SHIFT_BTTN(String text, int factor) {
            super(text);
            this.m_Factor = factor;
            this.setMargin(new Insets(0, 0, 0, 0));
            this.setFont(new Font(this.getFont().getName(), 0, 10));
            TimeShiftControl.this.getSelection().getRegister().add(new PRT_SEL_CLNT());
        }

        public void actionPerformed(ActionEvent e) {
            for (final Track p : TimeShiftControl.this.getSelection()) {
                p.shift(((long) TimeShiftControl.this.m_TimeShift.getTicks() * this.m_Factor));
            }
        }

        private class PRT_SEL_CLNT implements Listener<Selection.Message<Track>> {
            private PRT_SEL_CLNT() {
            }

            public void pass(Selection.Message<Track> message) {
                SHIFT_BTTN.this.setEnabled(1 <= message.getSender().size());
            }
        }
    }

    private class TIMESHIFT extends TimeShiftBase {
        private TIMESHIFT() {
        }

        protected Timing getTiming() {
            return TimeShiftControl.this.getSequence().getTiming();
        }
    }
}
