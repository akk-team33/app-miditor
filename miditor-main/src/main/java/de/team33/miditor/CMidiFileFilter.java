package de.team33.miditor;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class CMidiFileFilter extends FileFilter {
    private static final String[] EXTENSION = new String[]{"mid", "midi"};
    private static final String DESCRIPTION = "MIDI Standard File (*.mid, *.midi)";

    public CMidiFileFilter() {
    }

    public final boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        } else {
            String fExt = f.getName().replaceAll("[^.]*[.]", "");
            String[] var6;
            int var5 = (var6 = EXTENSION).length;

            for (int var4 = 0; var4 < var5; ++var4) {
                String mExt = var6[var4];
                if (fExt.equalsIgnoreCase(mExt)) {
                    return true;
                }
            }

            return false;
        }
    }

    public final String getDescription() {
        return "MIDI Standard File (*.mid, *.midi)";
    }
}
