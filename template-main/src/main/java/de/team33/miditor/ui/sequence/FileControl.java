package de.team33.miditor.ui.sequence;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.team33.messaging.Listener;
import de.team33.midi.Sequence;
import net.team33.miditor.CMidiFileFilter;
import de.team33.miditor.ui.Rsrc;
import net.team33.swing.XButton;

public abstract class FileControl extends JPanel {
    public FileControl() {
        super(new GridLayout(1, 0, 1, 1));
        this.add(new SAVE_BTTN());
        this.add(new SVAS_BTTN());
    }

    protected abstract Context getContext();

    private abstract class BUTTON extends XButton {
        public BUTTON(Icon ico) {
            super(ico);
            this.setMargin(new Insets(1, 1, 1, 1));
        }
    }

    private class SAVE_BTTN extends BUTTON {
        private final SONG_CLIENT m_SongClient = new SONG_CLIENT();

        public SAVE_BTTN() {
            super(Rsrc.SAVEICON);
            this.setToolTipText("MIDI-Sequenz speichern");
            FileControl.this.getContext().getSequence().getRegister(Sequence.SetModified.class).add(this.m_SongClient);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                FileControl.this.getContext().getSequence().save();
            } catch (IOException var3) {
                JOptionPane.showMessageDialog(FileControl.this.getContext().getFrame(), "Die Datei\n\t" + FileControl.this.getContext().getSequence().getFile().getPath() + "\nkonnte nicht gespeichert werden.\n\nEventuell ist die Datei schreibgeschützt\noder Ihnen fehlen die notwendigen Rechte.", "Datei-Fehler", 0);
            }

        }

        protected void finalize() throws Throwable {
            FileControl.this.getContext().getSequence().getRegister(Sequence.SetModified.class).remove(this.m_SongClient);
            super.finalize();
        }

        private class SONG_CLIENT implements Listener<Sequence.SetModified> {
            private SONG_CLIENT() {
            }

            public void pass(Sequence.SetModified message) {
                boolean b = ((Sequence)message.getSender()).isModified();
                SAVE_BTTN.this.setEnabled(b);
            }
        }
    }

    private class SVAS_BTTN extends BUTTON {
        public SVAS_BTTN() {
            super(Rsrc.SVASICON);
            this.setToolTipText("MIDI-Sequenz speichern als ...");
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser(FileControl.this.getContext().getSequence().getFile().getParentFile());
            CMidiFileFilter filter = new CMidiFileFilter();
            chooser.setDialogTitle("Song speichern");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showSaveDialog(FileControl.this.getContext().getFrame());
            if (returnVal == 0) {
                try {
                    File f = chooser.getSelectedFile();
                    if (!filter.accept(f) && !f.isFile()) {
                        f = new File(f.getParentFile(), f.getName() + ".mid");
                    }

                    FileControl.this.getContext().getSequence().save_as(f);
                } catch (IOException var6) {
                    JOptionPane.showMessageDialog(FileControl.this.getContext().getFrame(), "Die Datei\n\t" + chooser.getSelectedFile() + "\nkonnte nicht gespeichert werden.\n\nEventuell ist die Datei schreibgeschützt\noder Ihnen fehlen die notwendigen Rechte.", "Datei-Fehler", 0);
                }
            }

        }
    }
}
