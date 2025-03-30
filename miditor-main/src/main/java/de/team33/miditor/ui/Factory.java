package de.team33.miditor.ui;

import de.team33.midi.Metronome;
import de.team33.midi.Part;
import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.JButtons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Factory {

    private final Context context;

    private Factory(final Context context) {
        this.context = context;
    }

    public static Factory by(final Context context) {
        return new Factory(context);
    }

    private static JButtons.Builder<?> stdButtonBuilder() {
        return JButtons.builder()
                       .setMargin(new Insets(1, 1, 1, 1));
    }

    public final JButton metronomeButton(final Supplier<? extends Metronome.Parameter> newMetronomeParameter) {
        return stdButtonBuilder().setIcon(Rsrc.METRONOM)
                                 .setToolTipText("create metronome part")
                                 .on(Event.ACTION_PERFORMED,
                                     ignored -> context.score().create(new Metronome(newMetronomeParameter.get())))
                                 .build();
    }

    private JButtons.Builder<?> shiftButtonBuilder(final long delta) {
        final Consumer<ActionEvent> consumer = ignored -> {
            for (final Part part : context.score().getParts()) {
                part.shift(delta);
            }
        };
        return stdButtonBuilder().on(Event.ACTION_PERFORMED, consumer);
    }

    public final JButton revBeatShiftButton() {
        final long delta = -context.score().timing().beatTicks();
        return shiftButtonBuilder(delta).setText("<")
                                        .setToolTipText("Move events one beat to the 'left'")
                                        .build();
    }

    public final JButton fwdBeatShiftButton() {
        final long delta = context.score().timing().beatTicks();
        return shiftButtonBuilder(delta).setText(">")
                                        .setToolTipText("Move events one beat to the 'right'")
                                        .build();
    }

    public final JButton revBarShiftButton() {
        final long delta = -context.score().timing().barTicks();
        return shiftButtonBuilder(delta).setText("<<")
                                        .setToolTipText("Move events one bar to the 'left'")
                                        .build();
    }

    public final JButton fwdBarShiftButton() {
        final long delta = context.score().timing().barTicks();
        return shiftButtonBuilder(delta).setText(">>")
                                        .setToolTipText("Move events one bar to the 'right'")
                                        .build();
    }
}
