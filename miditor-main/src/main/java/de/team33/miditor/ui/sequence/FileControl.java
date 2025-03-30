package de.team33.miditor.ui.sequence;

import de.team33.midi.Score;
import de.team33.miditor.CMidiFileFilter;
import de.team33.miditor.ui.Rsrc;
import de.team33.swing.XButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public final class FileControl extends JPanel {

    private final Context context;

    public FileControl(final Context context) {
        super(new GridLayout(1, 0, 1, 1));
        this.context = context;
        add(new SAVE_BTTN());
        add(new SVAS_BTTN());
    }

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
            context.score().registry().add(Score.Channel.SetModified, this::onSetModified);
        }

        public final void actionPerformed(final ActionEvent e) {
            try {
                context.music().save();
            } catch (final IOException var3) {
                JOptionPane.showMessageDialog(context.window(),
                                              "Die Datei\n\t" +
                                              context.music().path() +
                                              "\nkonnte nicht gespeichert werden.\n" +
                                              "\nEventuell ist die Datei schreibgeschützt" +
                                              "\noder Ihnen fehlen die notwendigen Rechte.",
                                              "Datei-Fehler", 0);
            }

        }

//        protected void finalize() throws Throwable {
//            context.getSequence().getRegister(Sequence.SetModified.class).remove(m_SongClient);
//            super.finalize();
//        }

        private void onSetModified(final Score sequence) {
            final boolean b = sequence.isModified();
            setEnabled(b);
        }
    }

    private class SVAS_BTTN extends BUTTON {
        public SVAS_BTTN() {
            super(Rsrc.SVASICON);
            setToolTipText("MIDI-Sequenz speichern als ...");
        }

        public final void actionPerformed(final ActionEvent e) {
            final JFileChooser chooser = new JFileChooser(context.music().path().getParent().toFile());
            final CMidiFileFilter filter = new CMidiFileFilter();
            chooser.setDialogTitle("Song speichern");
            chooser.setFileFilter(filter);
            final int returnVal = chooser.showSaveDialog(context.window());
            if (returnVal == 0) {
                try {
                    File f = chooser.getSelectedFile();
                    if (!filter.accept(f) && !f.isFile()) {
                        f = new File(f.getParentFile(), f.getName() + ".mid");
                    }

                    context.music().saveAs(f.toPath());
                } catch (final IOException var6) {
                    JOptionPane.showMessageDialog(context.window(),
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
