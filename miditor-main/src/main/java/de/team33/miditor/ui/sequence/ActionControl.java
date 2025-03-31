package de.team33.miditor.ui.sequence;

import de.team33.midi.Metronome;
import de.team33.miditor.ui.Basics;
import de.team33.miditor.ui.Rsrc;
import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.JButtons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public final class ActionControl extends JPanel {

    private final Context context;

    public ActionControl(final Context context) {
        super(new GridLayout(1, 0, 1, 1));
        this.context = context;

        add(metronomeButton());
        add(revShiftBarButton());
        add(revShiftBeatButton());
        add(fwdShiftBeatButton());
        add(fwdShiftBarButton());
    }

    private JButton metronomeButton() {
        final Metronome.Parameter parameter = newMetronomeParameter();
        final Consumer<ActionEvent> action = any -> context.score().create(new Metronome(parameter));
        return stdButton(action).setIcon(Rsrc.METRONOM)
                                .setToolTipText("Create a metronome 'part'")
                                .build();
    }

    private Metronome.Parameter newMetronomeParameter() {
        //noinspection MagicNumber
        return new Metronome.Parameter(context.timing(), 0,
                                       context.score().getTickLength(), 9, 76, 77, 127, 95);
    }

    private JButton fwdShiftBeatButton() {
        final int delta = context.timing().beatTicks();
        return shiftButton(delta).setText(">")
                                 .setToolTipText("Move events one beat to the 'right'")
                                 .build();
    }

    private JButton fwdShiftBarButton() {
        final int delta = context.timing().barTicks();
        return shiftButton(delta).setText(">>")
                                 .setToolTipText("Move events one bar to the 'right'")
                                 .build();
    }

    private JButton revShiftBeatButton() {
        final int delta = -context.timing().beatTicks();
        return shiftButton(delta).setText("<")
                                 .setToolTipText("Move events one beat to the 'left'")
                                 .build();
    }

    private JButton revShiftBarButton() {
        final int delta = -context.timing().barTicks();
        return shiftButton(delta).setText("<<")
                                 .setToolTipText("Move events one bar to the 'left'")
                                 .build();
    }

    private JButtons.Builder<?> shiftButton(final long delta) {
        return stdButton(any -> context.score()
                                       .tracks()
                                       .forEach(part -> part.shift(delta)));
    }

    private static JButtons.Builder<?> stdButton(final Consumer<ActionEvent> action) {
        return Basics.buttonBuilder()
                     .on(Event.ACTION_PERFORMED, action);
    }
}
