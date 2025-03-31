package de.team33.miditor.ui.sequence;

import de.team33.midi.Metronome;
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
        return new Metronome.Parameter() {

            public int getChannel() {
                return 9;
            }

            public int getDynamic(final long pos) {
                return 112;
            }

            public long getMax() {
                return context.score().getTickLength();
            }

            public long getMin() {
                return 0L;
            }

            public int getNoteNo(final long pos) {
                return pos % (long) context.score().getTiming().barTicks() == 0L ? 76 : 77;
            }

            public int getRes() {
                return context.score().getTiming().beatTicks();
            }
        };
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
        return JButtons.builder()
                       .setMargin(new Insets(1, 1, 1, 1))
                       .on(Event.ACTION_PERFORMED, action);
    }
}
