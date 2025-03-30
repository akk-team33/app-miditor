package de.team33.miditor.ui.sequence;

import de.team33.midi.Metronome;
import de.team33.miditor.ui.Factory;

import javax.swing.*;
import java.awt.*;

public final class ActionControl extends JPanel {

    private final Context context;

    public ActionControl(final Context context) {
        super(new GridLayout(1, 0, 1, 1));
        this.context = context;
        final Factory factory = Factory.by(context);
        add(factory.metronomeButton(this::newMetronomeParameter));
        add(factory.revBarShiftButton());
        add(factory.revBeatShiftButton());
        add(factory.fwdBeatShiftButton());
        add(factory.fwdBarShiftButton());
    }

    private Metronome.Parameter newMetronomeParameter() {
        //noinspection MagicNumber
        return new Metronome.Parameter(context.score().timing(), 0,
                                       context.score().getTickLength(), 9, 76, 77, 127, 95);
    }
}
