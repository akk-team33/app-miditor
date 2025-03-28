package de.team33.miditor.ui.sequence;

import de.team33.midi.Metronome;
import de.team33.midi.Part;
import de.team33.miditor.ui.Rsrc;
import de.team33.swing.XButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public abstract class ActionControl extends JPanel {
    public ActionControl() {
        super(new GridLayout(1, 0, 1, 1));
        add(new CNT_IN_BTTN());
        add(new REVMESURE_BTTN());
        add(new REVBEAT_BTTN());
        add(new FWDBEAT_BTTN());
        add(new FWDMESURE_BTTN());
    }

    protected abstract Context getContext();

    private abstract class BUTTON extends XButton {
        public BUTTON(final Icon ico) {
            super(ico);
            setMargin(new Insets(1, 1, 1, 1));
        }
    }

    private class CNT_IN_BTTN extends BUTTON {
        public CNT_IN_BTTN() {
            super(Rsrc.METRONOM);
            setToolTipText("Metronom-Spur anlegen");
        }

        public void actionPerformed(final ActionEvent e) {
            getContext().getSequence().create(new Metronome(newMetronomeParameter()));
        }
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
                return getContext().getSequence().getTickLength();
            }

            public long getMin() {
                return 0L;
            }

            public int getNoteNo(final long pos) {
                return pos % (long) getContext().getSequence().getTiming().barTicks() == 0L ? 76 : 77;
            }

            public int getRes() {
                return getContext().getSequence().getTiming().beatTicks();
            }
        };
    }

    private class FWDBEAT_BTTN extends SHIFT_BTTN {
        public FWDBEAT_BTTN() {
            super(">");
            setToolTipText("Events um einen Schlag nach 'rechts' verschieben");
        }

        protected long getDelta() {
            return getContext().getSequence().getTiming().beatTicks();
        }
    }

    private class FWDMESURE_BTTN extends SHIFT_BTTN {
        public FWDMESURE_BTTN() {
            super(">>");
            setToolTipText("Events um einen Takt nach 'rechts' verschieben");
        }

        protected long getDelta() {
            return getContext().getSequence().getTiming().barTicks();
        }
    }

    private class REVBEAT_BTTN extends SHIFT_BTTN {
        public REVBEAT_BTTN() {
            super("<");
            setToolTipText("Events um einen Schlag nach 'links' verschieben");
        }

        protected long getDelta() {
            return -(long) getContext().getSequence().getTiming().beatTicks();
        }
    }

    private class REVMESURE_BTTN extends SHIFT_BTTN {
        public REVMESURE_BTTN() {
            super("<<");
            setToolTipText("Events um einen Takt nach 'links' verschieben");
        }

        protected long getDelta() {
            return -(long) getContext().getSequence().getTiming().barTicks();
        }
    }

    private abstract class SHIFT_BTTN extends XButton {
        public SHIFT_BTTN(final String text) {
            super(text);
            setMargin(new Insets(1, 1, 1, 1));
        }

        protected abstract long getDelta();

        public void actionPerformed(final ActionEvent e) {
            final List<Part> var5;
            final int var4 = (var5 = getContext().getSequence().getTracks()).size();

            for (int var3 = 0; var3 < var4; ++var3) {
                final Part t = var5.get(var3);
                t.shift(getDelta());
            }
        }
    }
}
