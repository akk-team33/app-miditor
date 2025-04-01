package de.team33.miditor.ui.player;

import de.team33.midi.Player;
import de.team33.miditor.ui.Basics;
import de.team33.miditor.ui.Context;
import de.team33.miditor.ui.Rsrc;
import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.JPanels;

import javax.swing.*;

public final class DriveControl {

    private final Context context;

    private DriveControl(final Context context) {
        this.context = context;
    }

    public static JPanel by(final Context context) {
        return new DriveControl(context).build();
    }

    private static JButton locatorButton(final Icon ico, final Runnable action) {
        return Basics.iconButton(ico)
                     .on(Event.ACTION_PERFORMED, any -> action.run())
                     .build();
    }

    private static void onSetState(final JButton button, final Player.Trigger trigger, final Player.State state) {
        button.setEnabled(Player.Trigger.effectiveOn(state).contains(trigger));
        final JRootPane rp = button.getRootPane();
        if (null != rp) {
            if ((Player.Trigger.START == trigger) && (Player.State.RUNNING != state)) {
                rp.setDefaultButton(button);
                button.requestFocus();
            }

            if ((Player.Trigger.STOP == trigger) && (Player.State.RUNNING == state)) {
                rp.setDefaultButton(button);
            }

            if ((Player.Trigger.PAUSE == trigger) && (Player.State.RUNNING == state)) {
                button.requestFocus();
            }
        }
    }

    private JPanel build() {
        return JPanels.builder()
                      .add(rewButton())
                      .add(triggerButton(Player.Trigger.START))
                      .add(triggerButton(Player.Trigger.PAUSE))
                      .add(triggerButton(Player.Trigger.STOP))
                      .add(fwdButton())
                      .build();
    }

    private JButton fwdButton() {
        return locatorButton(Rsrc.DC_FWDICON, this::forwardBar);
    }

    private JButton rewButton() {
        return locatorButton(Rsrc.DC_REWICON, this::rewindBar);
    }

    private void forwardBar() {
        final long barTicks = context.timing().barTicks();
        final long oldPosition = context.player().getPosition();
        final long newPosition = ((oldPosition / barTicks) + 1) * barTicks;
        context.player().setPosition(newPosition);
    }

    private void rewindBar() {
        final long barTicks = context.timing().barTicks();
        final long beatTicks = context.timing().beatTicks();
        final long oldPosition = context.player().getPosition();
        final long newPosition = ((oldPosition - beatTicks) / barTicks) * barTicks;
        context.player().setPosition(newPosition);
    }

    private JButton triggerButton(final Player.Trigger trigger) {
        return Basics.iconButton(Rsrc.dcIcon(trigger))
                     .setup(button -> context.player().registry()
                                             .add(Player.Channel.SET_STATE,
                                                  state -> onSetState(button, trigger, state)))
                     .on(Event.ACTION_PERFORMED, any -> context.player().push(trigger))
                     .build();
    }
}
