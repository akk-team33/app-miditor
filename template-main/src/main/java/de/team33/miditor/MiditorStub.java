package de.team33.miditor;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.team33.miditor.visible.MainFrame;
import net.team33.midi.impl.SequenceImpl;

public class MiditorStub implements Runnable {
    private static final Preferences PREFS = Preferences.userRoot().node("net.team33/Miditor");
    private final String[] args;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new MiditorStub(args));
    }

    public MiditorStub(String[] args) {
        this.args = args;
    }

    public void run() {
        if (this.args.length < 1) {
            JOptionPane.showMessageDialog((Component)null, "Diese Anwendung erwartet den Pfad/Dateinamen einer MIDI-Datei als Parameter.", "Keine Datei angegeben", 0);
        } else {
            String[] var4;
            int var3 = (var4 = this.args).length;

            for(int var2 = 0; var2 < var3; ++var2) {
                String arg = var4[var2];
                File f = new File(arg);

                try {
                    (new MainFrame(new SequenceImpl(f), PREFS.node("MainFrame"))).setVisible(true);
                } catch (MidiUnavailableException var7) {
                    JOptionPane.showMessageDialog((Component)null, "Auf diesem System steht Ihnen anscheinend\nleider kein MIDI Subsystem zur Verfügung.\n\nDie Datei\n\t" + arg + "\nkonnte nicht geöffnet werden.", "MIDI-System fehlt", 0);
                } catch (InvalidMidiDataException var8) {
                    JOptionPane.showMessageDialog((Component)null, "Die Datei\n\t" + arg + "\nentspricht anscheinend nicht dem Standard MIDI File Format.\n\nEventuell lässt sie sich aber mit Hilfe eines anderen Tools konvertieren.", "Dateiformat unbekannt", 0);
                } catch (IOException var9) {
                    JOptionPane.showMessageDialog((Component)null, "Beim Zugriff auf\n\t" + arg + "\nist ein Fehler aufgetreten.\n\n(Datei existiert nicht, ist geschützt oder nicht lesbar).", "Datei Zugriffsfehler", 0);
                }
            }
        }
    }
}
