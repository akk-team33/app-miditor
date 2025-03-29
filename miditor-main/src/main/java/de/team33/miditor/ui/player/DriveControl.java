package de.team33.miditor.ui.player;

import de.team33.midi.Player;
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
        add(new SBUTTON(Player.Trigger.START));
        add(new SBUTTON(Player.Trigger.PAUSE));
        add(new SBUTTON(Player.Trigger.STOP));
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
            final long ticksPerMeasure = getContext().getTiming().barTicks();
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
            final long ticksPerMeasure = getContext().getTiming().barTicks();
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
        private final Player.Trigger trigger;

        SBUTTON(final Player.Trigger trigger) {
            super(Rsrc.dcIcon(trigger));
            this.trigger = trigger;
            getContext().getPlayer().registry()
                        .add(Player.Channel.SET_STATE, this::onSetState);
            getContext().getWindow()
                        .addWindowListener(new CLIENT3());
            addActionListener(this::onActionPerformed);
        }

        private void _setState(final Player.State state) {
            synchronized (this) {
                setEnabled(Player.Trigger.effectiveOn(state).contains(trigger));
                final JRootPane rp = getRootPane();
                if (null != rp) {
                    if ((Player.Trigger.START == trigger) && (Player.State.RUNNING != state)) {
                        rp.setDefaultButton(this);
                        requestFocus();
                    }

                    if ((Player.Trigger.STOP == trigger) && (Player.State.RUNNING == state)) {
                        rp.setDefaultButton(this);
                    }

                    if ((Player.Trigger.PAUSE == trigger) && (Player.State.RUNNING == state)) {
                        requestFocus();
                    }
                }
            }
        }

        private void onActionPerformed(final ActionEvent e) {
            getContext().getPlayer().push(trigger);
        }

        private void onSetState(final Player.State state) {
            _setState(state);
        }

        private class CLIENT3 extends WindowAdapter {
            public void windowOpened(final WindowEvent e) {
                _setState(getContext().getPlayer().getState());
            }
        }
    }
}
