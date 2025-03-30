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

    @SuppressWarnings({"AnonymousInnerClassWithTooManyMethods", "OverlyComplexAnonymousInnerClass"})
    private Metronome.Parameter newMetronomeParameter() {
        return new Metronome.Parameter() {

            public int getChannel() {
                return 9;
            }

            @SuppressWarnings("MagicNumber")
            public int getDynamic(final long pos) {
                return 112;
            }

            public long getMax() {
                return context.score().getTickLength();
            }

            public long getMin() {
                return 0L;
            }

            @SuppressWarnings("MagicNumber")
            public int getNoteNo(final long pos) {
                return pos % context.score().timing().barTicks() == 0L ? 76 : 77;
            }

            public int getRes() {
                return context.score().timing().beatTicks();
            }
        };
    }
}
