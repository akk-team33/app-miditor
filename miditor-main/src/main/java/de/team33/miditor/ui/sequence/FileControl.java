package de.team33.miditor.ui.sequence;

import de.team33.midi.MidiSequence;
import de.team33.miditor.CMidiFileFilter;
import de.team33.miditor.ui.Rsrc;
import de.team33.swing.XButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public abstract class FileControl extends JPanel {
    public FileControl() {
        super(new GridLayout(1, 0, 1, 1));
        add(new SAVE_BTTN());
        add(new SVAS_BTTN());
    }

    protected abstract Context getContext();

    private abstract class BUTTON extends XButton {
        public BUTTON(final Icon ico) {
            super(ico);
            setMargin(new Insets(1, 1, 1, 1));
        }
    }

    private class SAVE_BTTN extends BUTTON {

        public SAVE_BTTN() {
            super(Rsrc.SAVEICON);
            setToolTipText("MIDI-Sequenz speichern");
            getContext().getSequence().registry().add(MidiSequence.Channel.SetModified, this::onSetModified);
        }

        public void actionPerformed(final ActionEvent e) {
            try {
                getContext().getSequence().save();
            } catch (final IOException var3) {
                JOptionPane.showMessageDialog(getContext().getFrame(),
                                              "Die Datei\n\t" +
                                              getContext().getSequence().getPath() +
                                              "\nkonnte nicht gespeichert werden.\n" +
                                              "\nEventuell ist die Datei schreibgeschützt" +
                                              "\noder Ihnen fehlen die notwendigen Rechte.",
                                              "Datei-Fehler", 0);
            }

        }

//        protected void finalize() throws Throwable {
//            getContext().getSequence().getRegister(Sequence.SetModified.class).remove(m_SongClient);
//            super.finalize();
//        }

        private void onSetModified(final MidiSequence sequence) {
            final boolean b = sequence.isModified();
            setEnabled(b);
        }
    }

    private class SVAS_BTTN extends BUTTON {
        public SVAS_BTTN() {
            super(Rsrc.SVASICON);
            setToolTipText("MIDI-Sequenz speichern als ...");
        }

        public void actionPerformed(final ActionEvent e) {
            final JFileChooser chooser = new JFileChooser(getContext().getSequence().getPath().getParent().toFile());
            final CMidiFileFilter filter = new CMidiFileFilter();
            chooser.setDialogTitle("Song speichern");
            chooser.setFileFilter(filter);
            final int returnVal = chooser.showSaveDialog(getContext().getFrame());
            if (returnVal == 0) {
                try {
                    File f = chooser.getSelectedFile();
                    if (!filter.accept(f) && !f.isFile()) {
                        f = new File(f.getParentFile(), f.getName() + ".mid");
                    }

                    getContext().getSequence().saveAs(f.toPath());
                } catch (final IOException var6) {
                    JOptionPane.showMessageDialog(getContext().getFrame(),
                                                  "Die Datei\n\t" +
                                                  chooser.getSelectedFile() +
                                                  "\nkonnte nicht gespeichert werden.\n" +
                                                  "\nEventuell ist die Datei schreibgeschützt\n" +
                                                  "oder Ihnen fehlen die notwendigen Rechte.",
                                                  "Datei-Fehler", 0);
                }
            }

        }
    }
}
