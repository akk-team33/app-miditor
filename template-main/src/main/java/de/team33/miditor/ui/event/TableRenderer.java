package de.team33.miditor.ui.event;

import java.awt.Component;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import de.team33.midi.Timing;

public abstract class TableRenderer extends DefaultTableCellRenderer {
    private static final String[] chnlType = new String[]{"NoteOff", "NoteOn", "PPress", "Control", "Program", "CPress", "PtchBnd"};
    private static final String[] noteName = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H"};
    private static final String[] sysSpec = new String[]{"EXCL", "MTC/s", "SngPos", "SngSel", "F4h", "F5h", "TuneReq", "EOX", "Clock", "F9h", "Strt", "Cont", "Stop", "FDh", "ActSens", "META"};

    public TableRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof MidiEvent) {
            value = this.getValue((MidiEvent)value, table.convertColumnIndexToModel(column));
        }

        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    protected abstract Timing getTiming();

    private Object getValue(MidiEvent event, int col) {
        return col == 0 ? this.getTiming().getTimeCode(event.getTick()) : this.getValue(event.getMessage(), col);
    }

    private Object getValue(MidiMessage message, int col) {
        return this.getValue(message.getStatus(), message.getMessage(), col);
    }

    private Object getValue(int status, byte[] data, int col) {
        return this.getValue(status & 240, status & 15, data, col);
    }

    private Object getValue(int type, int spec, byte[] data, int col) {
        if (col == 1) {
            return this.getChannel(type, spec);
        } else {
            return type == 240 ? this.getSysValue(spec, data, col) : this.getChnlValue(type, spec, data, col);
        }
    }

    private String getChannel(int type, int spec) {
        return type == 240 ? "Sys" : String.format("%02d", spec + 1);
    }

    private Object getChnlValue(int type, int spec, byte[] data, int col) {
        return col == 2 ? chnlType[type >> 4 & 7] : this.getChnlData(type, data, col);
    }

    private Object getChnlData(int type, byte[] data, int col) {
        switch (type) {
        case 128:
        case 144:
        case 160:
            return this.getScaleData(type, data, col);
        case 176:
            return this.getControlData(data, col);
        default:
            return this.getDefault(data, col);
        }
    }

    private String getControlData(byte[] data, int col) {
        switch (col) {
        case 3:
            return this.getControlName(data[1]);
        default:
            return this.getDefault(data, col);
        }
    }

    private String getControlName(int b) {
        switch (b) {
        case 7:
            return "VOL";
        case 8:
            return "BAL";
        case 10:
            return "PAN";
        case 39:
            return "vol";
        case 40:
            return "bal";
        case 42:
            return "pan";
        default:
            return this.getHex(b);
        }
    }

    private Object getScaleData(int type, byte[] data, int col) {
        switch (col) {
        case 3:
            return this.getNoteNumber(data[1]);
        case 4:
            if (type == 144 && data[2] == 0) {
                return "Off";
            }

            return this.getDecimal(data[2]);
        default:
            return this.getDefault(data, col);
        }
    }

    private Object getNoteNumber(int b) {
        int idx = b % 12;
        int octave = b / 12;
        return noteName[idx] + octave;
    }

    private String getSysValue(int spec, byte[] data, int col) {
        return col == 2 ? sysSpec[spec] : this.getSysData(spec, data, col);
    }

    private String getSysData(int spec, byte[] data, int col) {
        switch (spec) {
        case 15:
            return this.getMetaData(data, col);
        default:
            return this.getDefault(data, col);
        }
    }

    private String getDefault(byte[] data, int col) {
        switch (col) {
        case 3:
            if (data.length > 1) {
                return this.getDecimal(data[1]);
            }
            break;
        case 4:
            if (data.length > 2) {
                return this.getDecimal(data[2]);
            }
            break;
        case 5:
            return this.getRawData(data);
        }

        return "-";
    }

    private String getMetaData(byte[] data, int col) {
        if (data.length < 3) {
            return this.getDefault(data, col);
        } else {
            switch (col) {
            case 3:
                return this.getMetaType(data[1]);
            case 4:
                return this.getMetaContent(data);
            default:
                return this.getDefault(data, col);
            }
        }
    }

    private String getMetaContent(byte[] data) {
        switch (data[1]) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            return new String(data, 3, data.length - 3);
        default:
            return String.format("%03d", data[2]);
        }
    }

    private String getDecimal(int b) {
        return String.format("%03d", b);
    }

    private String getMetaType(int b) {
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
            return "SeqSpec";
        default:
            return this.getHex(b);
        }
    }

    private String getHex(int b) {
        return String.format("%02Xh", b);
    }

    private String getRawData(byte[] data) {
        String ret = "";
        byte[] var6 = data;
        int var5 = data.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            byte b = var6[var4];
            ret = ret + String.format(" %02X", b);
        }

        return ret.trim();
    }
}
