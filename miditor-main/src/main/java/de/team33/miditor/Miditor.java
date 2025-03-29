package de.team33.miditor;

import de.team33.midi.Music;
import de.team33.miditor.ui.MainFrame;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class Miditor implements Runnable {
    private static final Preferences PREFS = Preferences.userRoot().node("de.team33/Miditor");
    private final String[] args;

    public Miditor(String[] args) {
        this.args = args;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Miditor(args));
    }

    public final void run() {
        if (this.args.length < 1) {
            JOptionPane.showMessageDialog(null, "Diese Anwendung erwartet den Pfad/Dateinamen einer MIDI-Datei als Parameter.", "Keine Datei angegeben", 0);
        } else {
            String[] var4;
            int var3 = (var4 = this.args).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                String arg = var4[var2];
                File f = new File(arg);

                try {
                    (new MainFrame(Music.loader(new SimpleAsyncExecutor()).load(f.toPath()), PREFS.node("MainFrame"))).setVisible(true);
                } catch (final RuntimeException e) {
                    JOptionPane.showMessageDialog(null, "Auf diesem System steht Ihnen anscheinend\nleider kein MIDI Subsystem zur Verfügung.\n\nDie Datei\n\t" + arg + "\nkonnte nicht geöffnet werden.", "MIDI-System fehlt", 0);
                } catch (final InvalidMidiDataException e) {
                    JOptionPane.showMessageDialog(null, "Die Datei\n\t" + arg + "\nentspricht anscheinend nicht dem Standard MIDI File Format.\n\nEventuell lässt sie sich aber mit Hilfe eines anderen Tools konvertieren.", "Dateiformat unbekannt", 0);
                } catch (final IOException var9) {
                    JOptionPane.showMessageDialog(null, "Beim Zugriff auf\n\t" + arg + "\nist ein Fehler aufgetreten.\n\n(Datei existiert nicht, ist geschützt oder nicht lesbar).", "Datei Zugriffsfehler", 0);
                }
            }
        }
    }
}
