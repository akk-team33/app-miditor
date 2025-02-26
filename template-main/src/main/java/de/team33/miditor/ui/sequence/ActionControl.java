package de.team33.miditor.ui.sequence;

import de.team33.midi.Track;
import de.team33.miditor.IClickParameter;
import de.team33.miditor.ui.Rsrc;
import de.team33.swing.XButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class ActionControl extends JPanel {
    public ActionControl() {
        super(new GridLayout(1, 0, 1, 1));
        this.add(new CNT_IN_BTTN());
        this.add(new REVMESURE_BTTN());
        this.add(new REVBEAT_BTTN());
        this.add(new FWDBEAT_BTTN());
        this.add(new FWDMESURE_BTTN());
    }

    protected abstract Context getContext();

    private abstract class BUTTON extends XButton {
        public BUTTON(Icon ico) {
            super(ico);
            this.setMargin(new Insets(1, 1, 1, 1));
        }
    }

    private class CNT_IN_BTTN extends BUTTON {
        public CNT_IN_BTTN() {
            super(Rsrc.METRONOM);
            this.setToolTipText("Metronom-Spur anlegen");
        }

        public void actionPerformed(ActionEvent e) {
            ActionControl.this.getContext().getSequence().create(ActionControl.this.new CP());
        }
    }

    private class CP implements IClickParameter {
        private CP() {
        }

        public int getChannel() {
            return 9;
        }

        public int getDynamic(long pos) {
            return 112;
        }

        public long getMax() {
            return ActionControl.this.getContext().getSequence().getTickLength();
        }

        public long getMin() {
            return 0L;
        }

        public int getNoteNo(long pos) {
            return pos % (long)ActionControl.this.getContext().getSequence().getTiming().getBarTicks() == 0L ? 76 : 77;
        }

        public int getRes() {
            return ActionControl.this.getContext().getSequence().getTiming().getBeatTicks();
        }
    }

    private class FWDBEAT_BTTN extends SHIFT_BTTN {
        public FWDBEAT_BTTN() {
            super(">");
            this.setToolTipText("Events um einen Schlag nach 'rechts' verschieben");
        }

        protected long getDelta() {
            return ActionControl.this.getContext().getSequence().getTiming().getBeatTicks();
        }
    }

    private class FWDMESURE_BTTN extends SHIFT_BTTN {
        public FWDMESURE_BTTN() {
            super(">>");
            this.setToolTipText("Events um einen Takt nach 'rechts' verschieben");
        }

        protected long getDelta() {
            return ActionControl.this.getContext().getSequence().getTiming().getBarTicks();
        }
    }

    private class REVBEAT_BTTN extends SHIFT_BTTN {
        public REVBEAT_BTTN() {
            super("<");
            this.setToolTipText("Events um einen Schlag nach 'links' verschieben");
        }

        protected long getDelta() {
            return -(long) ActionControl.this.getContext().getSequence().getTiming().getBeatTicks();
        }
    }

    private class REVMESURE_BTTN extends SHIFT_BTTN {
        public REVMESURE_BTTN() {
            super("<<");
            this.setToolTipText("Events um einen Takt nach 'links' verschieben");
        }

        protected long getDelta() {
            return -(long) ActionControl.this.getContext().getSequence().getTiming().getBarTicks();
        }
    }

    private abstract class SHIFT_BTTN extends XButton {
        public SHIFT_BTTN(String text) {
            super(text);
            this.setMargin(new Insets(1, 1, 1, 1));
        }

        protected abstract long getDelta();

        public void actionPerformed(ActionEvent e) {
            Track[] var5;
            int var4 = (var5 = ActionControl.this.getContext().getSequence().getTracks()).length;

            for(int var3 = 0; var3 < var4; ++var3) {
                Track t = var5[var3];
                t.shift(this.getDelta());
            }

        }
    }
}
