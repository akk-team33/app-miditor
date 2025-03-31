package de.team33.miditor.ui.sequence;

import de.team33.midi.Score;
import de.team33.miditor.CMidiFileFilter;
import de.team33.miditor.ui.Basics;
import de.team33.miditor.ui.Rsrc;
import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.JFileChoosers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public final class FileControl extends JPanel {

    private static final String SAVE_FAILD = "Die Datei%n%n" +
                                             "\t<%s>%n%n" +
                                             "konnte nicht gespeichert werden.%n%n" +
                                             "Eventuell ist die Datei schreibgeschützt%n" +
                                             "oder Ihnen fehlen die notwendigen Rechte.";
    private static final String SAVE_FAILD_HEAD = "Datei-Fehler";

    private final Context context;

    public FileControl(final Context context) {
        super(new GridLayout(1, 0, 1, 1));
        this.context = context;

        add(saveButton());
        add(saveAsButton());
    }

    private JButton saveButton() {
        return Basics.buttonBuilder()
                     .setIcon(Rsrc.SAVEICON)
                     .setToolTipText("MIDI-Sequenz speichern")
                     .setup(jButton -> context.score().registry()
                                              .add(Score.Channel.SetModified,
                                                   score -> jButton.setEnabled(score.isModified())))
                     .on(Event.ACTION_PERFORMED, this::onSave)
                     .build();
    }

    private void onSave(final ActionEvent event) {
        try {
            context.music().save();
        } catch (final IOException e) {
            JOptionPane.showMessageDialog(context.window(),
                                          SAVE_FAILD.formatted(context.music().path()),
                                          SAVE_FAILD_HEAD, 0);
        }
    }

    private JButton saveAsButton() {
        return Basics.buttonBuilder()
                     .setIcon(Rsrc.SVASICON)
                     .setToolTipText("MIDI-Sequenz speichern als ...")
                     .on(Event.ACTION_PERFORMED, this::onSaveAs)
                     .build();
    }

    private void onSaveAs(final ActionEvent event) {
        final CMidiFileFilter filter = new CMidiFileFilter();
        final File file0 = context.music().path().getParent().toFile();
        final JFileChooser chooser = JFileChoosers.builder()
                                                  .setCurrentDirectory(file0)
                                                  .setDialogTitle("Song speichern")
                                                  .setFileFilter(filter)
                                                  .build();
        final int returnVal = chooser.showSaveDialog(context.window());
        if (0 == returnVal) {
            try {
                File fileX = chooser.getSelectedFile();
                if (!filter.accept(fileX) && !fileX.isFile()) {
                    fileX = new File(fileX.getParentFile(), fileX.getName() + ".mid");
                }
                context.music().saveAs(fileX.toPath());
            } catch (final IOException var6) {
                JOptionPane.showMessageDialog(context.window(),
                                              SAVE_FAILD.formatted(chooser.getSelectedFile()),
                                              SAVE_FAILD_HEAD, 0);
            }
        }
    }
}
