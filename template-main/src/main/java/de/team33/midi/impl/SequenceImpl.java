package de.team33.midi.impl;

import de.team33.messaging.Register;
import de.team33.messaging.sync.Router;
import de.team33.messaging.util.ListenerUtil;
import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.midi.util.TrackUtil;
import de.team33.miditor.IClickParameter;
import de.team33.miditor.backend.Timing;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequenceImpl implements Sequence {
    private static final String ERR_BACKUP = "Die Datei '%1$s' konnte nicht umbenannt werden. Zuletzt wurde versucht, folgenden Namen zu verwenden: '%2$s'";
    private final Sequence.SetFile msgSetFile = new SET_FILE();
    private final Sequence.SetModified msgSetModified = new SET_MODIFIED();
    private final Sequence.SetParts msgSetParts = new SET_PARTS();
    private final Router<Sequence.Message> router = new Router();
    private final PART_MAP m_PartMap = new PART_MAP();
    private final javax.sound.midi.Sequence m_Sequence;
    protected int m_nSelectedTracks;
    private boolean m_isSessionFile = false;
    private boolean m_Modified = false;
    private PART[] m_Parts = null;
    private File m_File;
    private Timing m_Timing;

    public SequenceImpl(File file) throws InvalidMidiDataException, IOException {
        this.router.addInitials(Arrays.asList(this.msgSetFile, this.msgSetModified, this.msgSetParts));
        this.m_File = initialFile(file);
        this.m_Sequence = initialSequence(this.m_File);
        javax.sound.midi.Track[] var5;
        int var4 = (var5 = this.m_Sequence.getTracks()).length;

        for (int var3 = 0; var3 < var4; ++var3) {
            javax.sound.midi.Track tRaw = var5[var3];
            this.m_PartMap.put(tRaw, new PART(tRaw));
        }

        if (this.getTracks().length > 0) {
            this.m_Timing = Timing.of(getTimingEvent(this.getTracks()[0], 0L).getMessage(), m_Sequence);
        } else {
            this.m_Timing = Timing.of(m_Sequence);
        }

    }

    private static void _save(javax.sound.midi.Sequence s, File file, boolean doBackup) throws IOException {
        if (doBackup && file.exists()) {
            backup(file);
        }

        int mode = s.getTracks().length > 1 ? 1 : 0;
        MidiSystem.write(s, mode, file);
    }

    private static void backup(File file) throws IOException {
        String fmt = file.getPath().replaceAll(Pattern.quote("%"), Matcher.quoteReplacement("%%")).replaceAll("([.][^.]*)$", ".%03d$1");
        int i = 0;

        File fBak;
        for (fBak = new File(String.format(fmt, i)); fBak.exists(); fBak = new File(String.format(fmt, i))) {
            ++i;
            if (999 < i) {
                throw new BACKUP_OVERFLOW();
            }
        }

        if (!file.renameTo(fBak)) {
            String message = String.format("Die Datei '%1$s' konnte nicht umbenannt werden. Zuletzt wurde versucht, folgenden Namen zu verwenden: '%2$s'", file.getAbsolutePath(), fBak.getName());
            throw new BACKUP_RENAME(message);
        }
    }

    private static MidiEvent getMetaEvent(Track p, int type, long latestTick) {
        MidiEvent ret = null;
        int i = 0;

        for (int n = p.size(); i < n; ++i) {
            MidiEvent evnt = p.get(i);
            if (evnt.getTick() > latestTick) {
                break;
            }

            MidiMessage mssg = evnt.getMessage();
            if (mssg.getStatus() == 255) {
                byte[] b = mssg.getMessage();
                if (b.length > 2 && b[1] == type && b.length - b[2] == 3) {
                    ret = evnt;
                }
            }
        }

        return ret;
    }

    private static MidiEvent getTempoEvent(Track p, long latestTick) {
        return getMetaEvent(p, 81, latestTick);
    }

    private static MidiEvent getTimingEvent(Track p, long latestTick) {
        return getMetaEvent(p, 88, latestTick);
    }

    private static File initialFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException var2) {
            throw new RuntimeException("canonical(" + file.getPath() + ") not available", var2);
        }
    }

    private static javax.sound.midi.Sequence initialSequence(File file) throws InvalidMidiDataException, IOException {
        return MidiSystem.getSequence(file);
    }

    private void _save_and_relay(Set<Sequence.Message> messages) throws IOException {
        _save(this.m_Sequence, this.m_File, !this.m_isSessionFile);
        this.m_isSessionFile = true;
        this.core_setModified(false, messages);
        ListenerUtil.pass(this.router, messages);
    }

    public void associate(Sequencer s) throws InvalidMidiDataException {
        s.setSequence(this.m_Sequence);
    }

    private void core_clear(Set<Sequence.Message> messages) {
        if (this.m_Parts != null) {
            PART[] var5;
            int var4 = (var5 = this.m_Parts).length;

            for (int var3 = 0; var3 < var4; ++var3) {
                PART p = var5[var3];
                p.clrRegister();
            }

            this.m_Parts = null;
            messages.add(this.msgSetParts);
            this.core_setModified(true, messages);
        }

    }

    private Track core_create(Set<Sequence.Message> messages) {
        javax.sound.midi.Track rawTrack = this.m_Sequence.createTrack();
        if (rawTrack != null) {
            this.core_clear(messages);
            this.m_PartMap.put(rawTrack, new PART(rawTrack));
        }

        return (Track) this.m_PartMap.get(rawTrack);
    }

    private boolean core_delete(Track p, Set<Sequence.Message> messages) {
        boolean ret = false;
        if (p instanceof PART) {
            javax.sound.midi.Track tRaw = ((PART) p).getTrack();
            if (this.m_PartMap.containsKey(tRaw)) {
                ret = this.m_Sequence.deleteTrack(tRaw);
                if (ret) {
                    this.core_clear(messages);
                    this.m_PartMap.remove(tRaw);
                }
            }
        }

        return ret;
    }

    private void core_setFile(File f, Set<Sequence.Message> messages) {
        f = initialFile(f);
        if (!this.m_File.equals(f)) {
            this.m_File = f;
            messages.add(this.msgSetFile);
        }

    }

    private void core_setModified(boolean b, Set<Sequence.Message> messages) {
        if (this.m_Modified != b) {
            if (!(this.m_Modified = b)) {
                Iterator var4 = this.m_PartMap.keySet().iterator();

                while (var4.hasNext()) {
                    javax.sound.midi.Track tRaw = (javax.sound.midi.Track) var4.next();
                    ((PART) this.m_PartMap.get(tRaw)).setModified(false);
                }
            }

            messages.add(this.msgSetModified);
        }

    }

    public Track create() {
        Set<Sequence.Message> messages = new HashSet();
        Track ret = this.core_create(messages);
        ListenerUtil.pass(this.router, messages);
        return ret;
    }

    public Track create(IClickParameter cp) {
        Set<Sequence.Message> messages = new HashSet();
        Track ret = this.core_create(messages);
        MetaMessage msg0 = new MetaMessage();
        byte[] bytes = "Metronom".getBytes();

        try {
            msg0.setMessage(3, bytes, bytes.length);
            ret.add(new MidiEvent[]{new MidiEvent(msg0, 0L)});
        } catch (InvalidMidiDataException var14) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), var14);
        }

        for (long pos = cp.getMin(); pos <= cp.getMax(); pos += (long) cp.getRes()) {
            ShortMessage msg1 = new ShortMessage();
            ShortMessage msg2 = new ShortMessage();
            int data1 = cp.getNoteNo(pos);
            int data2 = cp.getDynamic(pos);

            try {
                msg1.setMessage(144, cp.getChannel(), data1, data2);
                msg2.setMessage(144, cp.getChannel(), data1, 0);
                ret.add(new MidiEvent[]{new MidiEvent(msg1, pos)});
                ret.add(new MidiEvent[]{new MidiEvent(msg2, pos + (long) (cp.getRes() / 4))});
            } catch (InvalidMidiDataException var13) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), var13);
            }
        }

        ListenerUtil.pass(this.router, messages);
        return ret;
    }

    public void delete(Iterable<Track> tracks) {
        Set<Sequence.Message> messages = new HashSet();
        Iterator var4 = tracks.iterator();

        while (var4.hasNext()) {
            Track p = (Track) var4.next();
            this.core_delete(p, messages);
        }

        ListenerUtil.pass(this.router, messages);
    }

    public boolean delete(Track track) {
        Set<Sequence.Message> messages = new HashSet();
        boolean ret = this.core_delete(track, messages);
        ListenerUtil.pass(this.router, messages);
        return ret;
    }

    public File getFile() {
        return this.m_File;
    }

    public Track[] getTracks() {
        if (this.m_Parts == null) {
            javax.sound.midi.Track[] rawTracks = this.m_Sequence.getTracks();
            this.m_Parts = new PART[rawTracks.length];

            for (int i = 0; i < rawTracks.length; ++i) {
                this.m_Parts[i] = (PART) this.m_PartMap.get(rawTracks[i]);
                this.m_Parts[i].getRegister(Track.SetModified.class).add(new PART_CLIENT());
            }
        }

        return this.m_Parts;
    }

    public <MSX extends Sequence.Message> Register<MSX> getRegister(Class<MSX> msgClass) {
        return this.router.getRegister(msgClass);
    }

    public int getTempo() {
        if (this.getTracks().length > 0) {
            MidiEvent evnt = getTempoEvent(this.getTracks()[0], 0L);
            if (evnt != null) {
                byte[] b = evnt.getMessage().getMessage();
                int mpqn = 0;

                for (int i = 3; i < 6; ++i) {
                    mpqn *= 256;
                    mpqn += b[i] & 255;
                }

                return (int) Math.round(6.0E7 / (double) mpqn);
            }
        }

        return 0;
    }

    public void setTempo(int tempo) {
        Track p0;
        if (this.getTracks().length > 0) {
            p0 = this.getTracks()[0];
        } else {
            p0 = this.create();
        }

        for (MidiEvent oldEvnt = getTempoEvent(p0, 0L); oldEvnt != null; oldEvnt = getTempoEvent(p0, 0L)) {
            p0.remove(new MidiEvent[]{oldEvnt});
        }

        if (tempo > 0) {
            long mpqn = Math.round(6.0E7 / (double) tempo);
            byte[] data = new byte[3];

            for (int i = 0; i < data.length; ++i) {
                data[2 - i] = (byte) ((int) mpqn);
                mpqn /= 256L;
            }

            MetaMessage mssg = new MetaMessage();

            try {
                mssg.setMessage(81, data, data.length);
            } catch (InvalidMidiDataException var9) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), var9);
            }

            MidiEvent evnt = new MidiEvent(mssg, 0L);
            p0.add(new MidiEvent[]{evnt});
        }

    }

    public long getTickLength() {
        return this.m_Sequence.getTickLength();
    }

    public Timing getTiming() {
        return this.m_Timing;
    }

    public boolean isModified() {
        return this.m_Modified;
    }

    public void join(Iterable<Track> tracks) {
        Set<Sequence.Message> messages = new HashSet();
        Track newTrack = this.core_create(messages);
        Iterator var5 = tracks.iterator();

        while (var5.hasNext()) {
            Track t = (Track) var5.next();
            newTrack.add(t.getAll());
            this.core_delete(t, messages);
        }

        ListenerUtil.pass(this.router, messages);
    }

    public void save() throws IOException {
        this._save_and_relay(new HashSet());
    }

    public void save_as(File f) throws IOException {
        Set<Sequence.Message> messages = new HashSet();
        f = initialFile(f);
        if (!f.equals(this.m_File)) {
            this.m_isSessionFile = false;
            this.core_setFile(f, messages);
        }

        this._save_and_relay(messages);
    }

    public void split(Track track) {
        Set<Sequence.Message> messages = new HashSet();
        Map<Integer, List<MidiEvent>> extract = track.extractChannels();
        Iterator var5 = extract.keySet().iterator();

        while (var5.hasNext()) {
            Integer channel = (Integer) var5.next();
            Track newTrack = this.core_create(messages);
            TrackUtil.add(newTrack, (Collection) extract.get(channel));
        }

        ListenerUtil.pass(this.router, messages);
    }

    private static class BACKUP_OVERFLOW extends IOException {
        public BACKUP_OVERFLOW() {
            super("Zu viele Backups!");
        }
    }

    private static class BACKUP_RENAME extends IOException {
        public BACKUP_RENAME(String message) {
            super(message);
        }
    }

    private static class PART_MAP extends HashMap<javax.sound.midi.Track, PART> {
        private PART_MAP() {
        }
    }

    private class MESSAGE implements Sequence.Message {
        private MESSAGE() {
        }

        public Sequence getSender() {
            return SequenceImpl.this;
        }
    }

    private class PART extends TrackBase {
        public PART(javax.sound.midi.Track t) {
            super(t);
        }

        protected void clrRegister() {
            super.clrRegister();
        }

        public int getIndex() {
            javax.sound.midi.Track[] t = SequenceImpl.this.m_Sequence.getTracks();

            for (int i = 0; i < t.length; ++i) {
                if (this == SequenceImpl.this.m_PartMap.get(t[i])) {
                    return i;
                }
            }

            return -1;
        }

        protected javax.sound.midi.Track getTrack() {
            return super.getTrack();
        }

        protected void setModified(boolean isModified) {
            super.setModified(isModified);
        }
    }

    private class PART_CLIENT implements Consumer<Track.SetModified> {
        private PART_CLIENT() {
        }

        public void accept(Track.SetModified message) {
            if (((Track) message.getSender()).isModified()) {
                Set<Sequence.Message> messages = new HashSet();
                SequenceImpl.this.core_setModified(true, messages);
                ListenerUtil.pass(SequenceImpl.this.router, messages);
            }

        }
    }

    private class SET_FILE extends MESSAGE implements Sequence.SetFile {
        private SET_FILE() {
            super();
        }
    }

    private class SET_MODIFIED extends MESSAGE implements Sequence.SetModified {
        private SET_MODIFIED() {
            super();
        }
    }

    private class SET_PARTS extends MESSAGE implements Sequence.SetParts {
        private SET_PARTS() {
            super();
        }
    }
}
