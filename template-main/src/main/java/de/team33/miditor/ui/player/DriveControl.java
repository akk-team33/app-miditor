package de.team33.miditor.ui.player;

import de.team33.midi.MidiPlayer;
import de.team33.midi.MidiPlayer.State;
import de.team33.miditor.ui.Rsrc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class DriveControl extends JPanel {
    public DriveControl() {
        super(new GridLayout(1, 0, 1, 1));
        add(new REW_BUTTON());
        add(new SBUTTON(State.RUN));
        add(new SBUTTON(State.PAUSE));
        add(new SBUTTON(State.STOP));
        add(new FWD_BUTTON());
    }

    protected abstract Context getContext();

    private static class ICOBUTTON extends JButton {
        public ICOBUTTON(final Icon ico) {
            setIcon(ico);
            setMargin(new Insets(1, 1, 1, 1));
        }
    }

    private class FWD_BUTTON extends LOC_BUTTON {
        public FWD_BUTTON() {
            super(Rsrc.DC_FWDICON);
        }

        protected void relocate() {
            final long ticksPerMeasure = getContext().getPlayer().getSequence().getTiming().barTicks();
            long threshold = 1L;
            threshold *= ticksPerMeasure;
            threshold /= 4L;
            long ticks = getContext().getPlayer().getPosition();
            ticks += threshold;
            ticks /= ticksPerMeasure;
            ++ticks;
            ticks *= ticksPerMeasure;
            getContext().getPlayer().setPosition(ticks);
        }
    }

    private abstract class LOC_BUTTON extends ICOBUTTON {
        public LOC_BUTTON(final Icon ico) {
            super(ico);
            addActionListener(new LISTENER());
        }

        protected abstract void relocate();

        private class LISTENER implements ActionListener {
            private LISTENER() {
            }

            public void actionPerformed(final ActionEvent e) {
                relocate();
            }
        }
    }

    private class REW_BUTTON extends LOC_BUTTON {
        public REW_BUTTON() {
            super(Rsrc.DC_REWICON);
        }

        protected void relocate() {
            final long ticksPerMeasure = getContext().getPlayer().getSequence().getTiming().barTicks();
            long threshold = 1L;
            threshold *= ticksPerMeasure;
            threshold *= 3L;
            threshold /= 4L;
            long ticks = getContext().getPlayer().getPosition();
            ticks += threshold;
            ticks /= ticksPerMeasure;
            --ticks;
            ticks *= ticksPerMeasure;
            getContext().getPlayer().setPosition(ticks);
        }
    }

    private class SBUTTON extends ICOBUTTON {
        private final MidiPlayer.State m_State;

        SBUTTON(final MidiPlayer.State s) {
            super(Rsrc.DC_ICONSET[s.ordinal()]);
            m_State = s;
            getContext().getPlayer()
                        .addListener(MidiPlayer.Event.SetState, this::onSetState);
            getContext().getWindow()
                        .addWindowListener(new CLIENT3());
            addActionListener(this::onActionPerformed);
        }

        private void _setState(final MidiPlayer.State state) {
            synchronized (this) {
                setEnabled(state != m_State);
                final JRootPane rp = getRootPane();
                if (null != rp) {
                    if ((State.RUN == m_State) && (State.RUN != state)) {
                        rp.setDefaultButton(this);
                        requestFocus();
                    }

                    if ((State.STOP == m_State) && (State.RUN == state)) {
                        rp.setDefaultButton(this);
                    }

                    if ((State.PAUSE == m_State) && (State.RUN == state)) {
                        requestFocus();
                    }
                }
            }
        }

        private void onActionPerformed(final ActionEvent e) {
            getContext().getPlayer().setState(m_State);
        }

        private void onSetState(final MidiPlayer player) {
            final MidiPlayer.State state = player.getState();
            _setState(state);
        }

        private class CLIENT3 extends WindowAdapter {
            public void windowOpened(final WindowEvent e) {
                _setState(getContext().getPlayer().getState());
            }
        }
    }
}
