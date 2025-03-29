package de.team33.miditor.ui.event;

import de.team33.midi.Timing;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.swing.*;
import java.awt.*;

public abstract class ListRenderer extends DefaultListCellRenderer {
    private static final String[] chnlType = new String[]{"NoteOff", "NoteOn", "PPress", "Control", "Program", "CPress", "PtchBnd"};
    private static final String[] noteName = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H"};
    private static final String[] sysSpec = new String[]{"SysEx", "MTC/short", "Song Position", "Song Select", "SYS(4)", "SYS(5)", "Tune Request", "EOX", "Timing Clock", "SYS(9)", "Start", "Continue", "Stop", "SYS(D)", "Active Sensing", "META"};

    public ListRenderer() {
    }

    public final Component getListCellRendererComponent(final JList list, Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        if (value instanceof MidiEvent) {
            value = this.getValue((MidiEvent) value);
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    protected abstract Timing getTiming();

    private String getValue(final MidiEvent event) {
        return String.format("%s : %s", this.getTiming().timeStampOf(event.getTick()), this.getValue(event.getMessage()));
    }

    private String getValue(final MidiMessage message) {
        return this.getValue(message.getStatus(), message.getMessage());
    }

    private String getValue(final int status, final byte[] data) {
        return String.format("%s %s %s %s", this.getHexValue(data, 0), this.getHexValue(data, 1), this.getHexValue(data, 2), this.getValue(status & 240, status & 15, data));
    }

    private String getValue(final int type, final int spec, final byte[] data) {
        return type == 240 ? this.getSysValue(spec, data) : this.getChnlValue(type, spec, data);
    }

    private String getChnlValue(final int type, final int spec, final byte[] data) {
        final String sType = chnlType[type >> 4 & 7];
        return String.format("%s(%s)%s Ch%02d", sType, this.getB1Value(type, data), this.getB2Value(type, data), spec + 1);
    }

    private String getB1Value(final int type, final byte[] data) {
        switch (type) {
        case 128:
        case 144:
        case 160:
            return this.getNoteNumber(data[1]);
        default:
            return String.format("%03d", data[1]);
        }
    }

    private String getNoteNumber(final int b) {
        final int idx = b % 12;
        final int octave = b / 12;
        return noteName[idx] + octave;
    }

    private String getB2Value(final int type, final byte[] data) {
        if (data.length < 3) {
            return "";
        } else {
            return type == 144 && data[2] == 0 ? " Off" : String.format(" %d", data[2]);
        }
    }

    private String getSysValue(final int spec, final byte[] data) {
        return String.format("%s %s", sysSpec[spec], this.getSysData(spec, data));
    }

    private String getSysData(final int spec, final byte[] data) {
        switch (spec) {
        case 15:
            return this.getMeta(data);
        default:
            return this.getRawData(data);
        }
    }

    private String getMeta(final byte[] data) {
        return data.length < 3 ? "(?)" : String.format(" %s[%d]: %s", this.getMetaType(data[1]), data[2], this.getMetaData(data));
    }

    private String getMetaData(final byte[] data) {
        switch (data[1]) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            return this.getMetaString(data);
        default:
            return this.getRawData(data);
        }
    }

    private String getMetaString(final byte[] data) {
        return "\"" + new String(data, 3, data.length - 3) + "\"";
    }

    private String getMetaType(final byte b) {
        switch (b) {
        case 0:
            return "SeqNo";
        case 1:
            return "Text";
        case 2:
            return "(C)";
        case 3:
            return "TrckName";
        case 4:
            return "InstName";
        case 5:
            return "Lyrics";
        case 6:
            return "Marker";
        case 7:
            return "CuePnt";
        case 32:
            return "ChanPrfx";
        case 47:
            return "EOT";
        case 81:
            return "Tempo";
        case 84:
            return "SMPTE Offset";
        case 88:
            return "Time Signature";
        case 89:
            return "Key Signature";
        case 127:
            return "SPEC";
        default:
            return this.getHex(b);
        }
    }

    private String getRawData(final byte[] data) {
        String ret = "";
        final byte[] var6 = data;
        final int var5 = data.length;

        for (int var4 = 0; var4 < var5; ++var4) {
            final byte b = var6[var4];
            ret = ret + String.format(" %02X", b);
        }

        return ret;
    }

    private String getHexValue(final byte[] data, final int i) {
        return data.length > i ? this.getHex(data[i]) : "--";
    }

    private String getHex(final byte b) {
        return String.format("%02X", b & 255);
    }
}
