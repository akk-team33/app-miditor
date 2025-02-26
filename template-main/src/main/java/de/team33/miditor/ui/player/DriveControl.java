package de.team33.miditor.ui.player;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import net.team33.messaging.Listener;
import net.team33.midi.Player;
import net.team33.midi.Player.State;
import de.team33.miditor.ui.Rsrc;

public abstract class DriveControl extends JPanel {
    public DriveControl() {
        super(new GridLayout(1, 0, 1, 1));
        this.add(new REW_BUTTON());
        this.add(new SBUTTON(State.RUN));
        this.add(new SBUTTON(State.PAUSE));
        this.add(new SBUTTON(State.STOP));
        this.add(new FWD_BUTTON());
    }

    protected abstract Context getContext();

    private class FWD_BUTTON extends LOC_BUTTON {
        public FWD_BUTTON() {
            super(Rsrc.DC_FWDICON);
        }

        protected void relocate() {
            long ticksPerMeasure = DriveControl.this.getContext().getPlayer().getSequence().getTiming().getBarTicks();
            long threshold = 1L;
            threshold *= ticksPerMeasure;
            threshold /= 4L;
            long ticks = DriveControl.this.getContext().getPlayer().getPosition();
            ticks += threshold;
            ticks /= ticksPerMeasure;
            ++ticks;
            ticks *= ticksPerMeasure;
            DriveControl.this.getContext().getPlayer().setPosition(ticks);
        }
    }

    private static class ICOBUTTON extends JButton {
        public ICOBUTTON(Icon ico) {
            this.setIcon(ico);
            this.setMargin(new Insets(1, 1, 1, 1));
        }
    }

    private abstract class LOC_BUTTON extends ICOBUTTON {
        public LOC_BUTTON(Icon ico) {
            super(ico);
            this.addActionListener(new LISTENER());
        }

        protected abstract void relocate();

        private class LISTENER implements ActionListener {
            private LISTENER() {
            }

            public void actionPerformed(ActionEvent e) {
                LOC_BUTTON.this.relocate();
            }
        }
    }

    private class REW_BUTTON extends LOC_BUTTON {
        public REW_BUTTON() {
            super(Rsrc.DC_REWICON);
        }

        protected void relocate() {
            long ticksPerMeasure = DriveControl.this.getContext().getPlayer().getSequence().getTiming().getBarTicks();
            long threshold = 1L;
            threshold *= ticksPerMeasure;
            threshold *= 3L;
            threshold /= 4L;
            long ticks = DriveControl.this.getContext().getPlayer().getPosition();
            ticks += threshold;
            ticks /= ticksPerMeasure;
            --ticks;
            ticks *= ticksPerMeasure;
            DriveControl.this.getContext().getPlayer().setPosition(ticks);
        }
    }

    private class SBUTTON extends ICOBUTTON {
        private final Player.State m_State;

        public SBUTTON(Player.State s) {
            super(Rsrc.DC_ICONSET[s.ordinal()]);
            this.m_State = s;
            DriveControl.this.getContext().getPlayer().getRegister(Player.SetState.class).add(new CLIENT2());
            DriveControl.this.getContext().getWindow().addWindowListener(new CLIENT3());
            this.addActionListener(new CLIENT1());
        }

        private synchronized void _setState(Player.State s) {
            this.setEnabled(s != this.m_State);
            JRootPane rp = this.getRootPane();
            if (rp != null) {
                if (this.m_State == State.RUN && s != State.RUN) {
                    rp.setDefaultButton(this);
                    this.requestFocus();
                }

                if (this.m_State == State.STOP && s == State.RUN) {
                    rp.setDefaultButton(this);
                }

                if (this.m_State == State.PAUSE && s == State.RUN) {
                    this.requestFocus();
                }

            }
        }

        private class CLIENT1 implements ActionListener {
            private CLIENT1() {
            }

            public void actionPerformed(ActionEvent e) {
                DriveControl.this.getContext().getPlayer().setState(SBUTTON.this.m_State);
            }
        }

        private class CLIENT2 implements Listener<Player.SetState> {
            private CLIENT2() {
            }

            public void pass(Player.SetState message) {
                Player.State s = message.getSender().getState();
                SBUTTON.this._setState(s);
            }
        }

        private class CLIENT3 extends WindowAdapter {
            private CLIENT3() {
            }

            public void windowOpened(WindowEvent e) {
                SBUTTON.this._setState(DriveControl.this.getContext().getPlayer().getState());
            }
        }
    }
}
