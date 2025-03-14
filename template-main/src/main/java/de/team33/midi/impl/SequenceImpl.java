package de.team33.midi.impl;

import de.team33.midi.MidiTrack;
import de.team33.midi.Sequence;
import de.team33.midi.util.TrackUtil;
import de.team33.midix.Timing;
import de.team33.patterns.notes.alpha.Audience;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequenceImpl implements Sequence {

    private static final String ERR_BACKUP =
            "Die Datei '%1$s' konnte nicht umbenannt werden. Zuletzt wurde versucht, folgenden Namen zu verwenden: '%2$s'";

    private final Audience audience = new Audience();
    private final PART_MAP m_PartMap = new PART_MAP();
    private final javax.sound.midi.Sequence m_Sequence;
    protected int m_nSelectedTracks;
    private boolean m_isSessionFile = false;
    private boolean m_Modified = false;
    private MidiTrack[] m_Parts = null;
    private File m_File;
    private Timing m_Timing;

    public SequenceImpl(final File file) throws InvalidMidiDataException, IOException {
        m_File = initialFile(file);
        m_Sequence = initialSequence(m_File);
        final javax.sound.midi.Track[] var5;
        final int length = (var5 = m_Sequence.getTracks()).length;

        for (int index = 0; index < length; ++index) {
            final javax.sound.midi.Track tRaw = var5[index];
            m_PartMap.put(tRaw, new MidiTrack(index, tRaw));
        }

        if (0 < getTracks().length) {
            this.m_Timing = Timing.of(getTimingEvent(this.getTracks()[0], 0L).getMessage(), m_Sequence);
        } else {
            this.m_Timing = Timing.of(m_Sequence);
        }
    }

    @Override
    public javax.sound.midi.Sequence backing() {
        return m_Sequence;
    }

    @Override
    public final void addListener(final Channel channel, final Consumer<? super Sequence> listener) {
        audience.add(channel, listener);
        listener.accept(this);
    }

    private static void _save(final javax.sound.midi.Sequence s, final File file, final boolean doBackup) throws IOException {
        if (doBackup && file.exists()) {
            backup(file);
        }
        final int mode = 1 < s.getTracks().length ? 1 : 0;
        MidiSystem.write(s, mode, file);
    }

    private static void backup(final File file) throws IOException {
        final String fmt = file.getPath().replaceAll(Pattern.quote("%"), Matcher.quoteReplacement("%%")).replaceAll("([.][^.]*)$", ".%03d$1");
        int i = 0;

        File fBak;
        for (fBak = new File(String.format(fmt, i)); fBak.exists(); fBak = new File(String.format(fmt, i))) {
            ++i;
            if (999 < i) {
                throw new BACKUP_OVERFLOW();
            }
        }

        if (!file.renameTo(fBak)) {
            final String message = String.format("Die Datei '%1$s' konnte nicht umbenannt werden. Zuletzt wurde versucht, folgenden Namen zu verwenden: '%2$s'", file.getAbsolutePath(), fBak.getName());
            throw new BACKUP_RENAME(message);
        }
    }

    private static MidiEvent getMetaEvent(final MidiTrack p, final int type, final long latestTick) {
        MidiEvent ret = null;
        int i = 0;

        for (final int n = p.size(); i < n; ++i) {
            final MidiEvent evnt = p.get(i);
            if (evnt.getTick() > latestTick) {
                break;
            }

            final MidiMessage mssg = evnt.getMessage();
            if (255 == mssg.getStatus()) {
                final byte[] b = mssg.getMessage();
                if (2 < b.length && b[1] == type && 3 == b.length - b[2]) {
                    ret = evnt;
                }
            }
        }

        return ret;
    }

    private static MidiEvent getTempoEvent(final MidiTrack p, final long latestTick) {
        return getMetaEvent(p, 81, latestTick);
    }

    private static MidiEvent getTimingEvent(final MidiTrack p, final long latestTick) {
        return getMetaEvent(p, 88, latestTick);
    }

    private static File initialFile(final File file) {
        try {
            return file.getCanonicalFile();
        } catch (final IOException var2) {
            throw new RuntimeException("canonical(" + file.getPath() + ") not available", var2);
        }
    }

    private static javax.sound.midi.Sequence initialSequence(final File file) throws InvalidMidiDataException, IOException {
        return MidiSystem.getSequence(file);
    }

    private void _save_and_relay(final Set<Channel> channels) throws IOException {
        _save(m_Sequence, m_File, !m_isSessionFile);
        m_isSessionFile = true;
        core_setModified(false, channels);
        channels.forEach(event -> audience.send(event, this));
    }

    private void core_clear(final Set<Channel> channels) {
        if (null != m_Parts) {
            m_Parts = null;
            channels.add(Channel.SetTracks);
            core_setModified(true, channels);
        }
    }

    private MidiTrack core_create(final Set<Channel> channels) {
        final javax.sound.midi.Track rawTrack = m_Sequence.createTrack();
        if (null != rawTrack) {
            core_clear(channels);
            m_PartMap.put(rawTrack, new MidiTrack(Arrays.asList(m_Sequence.getTracks()).indexOf(rawTrack), rawTrack));
        }

        return m_PartMap.get(rawTrack);
    }

    private boolean core_delete(final MidiTrack p, final Set<Channel> channels) {
        boolean ret = false;
        final javax.sound.midi.Track tRaw = p.backing();
        if (m_PartMap.containsKey(tRaw)) {
            ret = m_Sequence.deleteTrack(tRaw);
            if (ret) {
                core_clear(channels);
                m_PartMap.remove(tRaw);
            }
        }
        return ret;
    }

    private void core_setFile(File f, final Set<Channel> channels) {
        f = initialFile(f);
        if (!m_File.equals(f)) {
            m_File = f;
            channels.add(Channel.SetPath);
        }

    }

    private void core_setModified(final boolean b, final Set<Channel> channels) {
        if (m_Modified != b) {
            m_Modified = b;
            if (!m_Modified) {
                for (final MidiTrack part : m_PartMap.values()) {
                    part.resetModified();
                }
            }
            channels.add(Channel.SetModified);
        }
    }

    public final MidiTrack create(final Iterable<? extends MidiEvent> events) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final MidiTrack ret = core_create(channels);
        ret.add(events);
        channels.forEach(event -> audience.send(event, this));
        return ret;
    }

    public final void delete(final Iterable<MidiTrack> tracks) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        for (final MidiTrack p : tracks) {
            core_delete(p, channels);
        }
        channels.forEach(event -> audience.send(event, this));
    }

    public final boolean delete(final MidiTrack track) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final boolean ret = core_delete(track, channels);
        channels.forEach(event -> audience.send(event, this));
        return ret;
    }

    public final File getFile() {
        return m_File;
    }

    public final MidiTrack[] getTracks() {
        if (null == m_Parts) {
            final javax.sound.midi.Track[] rawTracks = m_Sequence.getTracks();
            m_Parts = new MidiTrack[rawTracks.length];
            for (int i = 0; i < rawTracks.length; ++i) {
                m_Parts[i] = m_PartMap.get(rawTracks[i]);
                m_Parts[i].add(MidiTrack.Channel.SetModified, this::onSetModified);
            }
        }
        return m_Parts;
    }

    public final int getTempo() {
        if (0 < getTracks().length) {
            final MidiEvent event = getTempoEvent(getTracks()[0], 0L);
            if (null != event) {
                final byte[] bytes = event.getMessage().getMessage();
                int mpqn = 0;

                for (int i = 3; 6 > i; ++i) {
                    mpqn *= 256;
                    mpqn += bytes[i] & 255;
                }

                return (int) Math.round(6.0E7 / (double) mpqn);
            }
        }

        return 0;
    }

    public final void setTempo(final int tempo) {
        final MidiTrack p0;
        if (0 < getTracks().length) {
            p0 = getTracks()[0];
        } else {
            p0 = create();
        }

        for (MidiEvent oldEvnt = getTempoEvent(p0, 0L); null != oldEvnt; oldEvnt = getTempoEvent(p0, 0L)) {
            p0.remove(oldEvnt);
        }

        if (0 < tempo) {
            long mpqn = Math.round(6.0E7 / (double) tempo);
            final byte[] data = new byte[3];

            for (int i = 0; i < data.length; ++i) {
                data[2 - i] = (byte) ((int) mpqn);
                mpqn /= 256L;
            }

            final MetaMessage mssg = new MetaMessage();

            try {
                mssg.setMessage(81, data, data.length);
            } catch (final InvalidMidiDataException var9) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), var9);
            }
            final MidiEvent evnt = new MidiEvent(mssg, 0L);
            p0.add(evnt);
        }
    }

    public final long getTickLength() {
        return m_Sequence.getTickLength();
    }

    public final Timing getTiming() {
        return m_Timing;
    }

    public final boolean isModified() {
        return m_Modified;
    }

    public final void join(final Iterable<MidiTrack> tracks) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final MidiTrack newTrack = core_create(channels);
        for (final MidiTrack t : tracks) {
            newTrack.add(t.list());
            core_delete(t, channels);
        }
        channels.forEach(event -> audience.send(event, this));
    }

    public final void save() throws IOException {
        _save_and_relay(new HashSet());
    }

    public final void save_as(File file) throws IOException {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        file = initialFile(file);
        if (!file.equals(m_File)) {
            m_isSessionFile = false;
            core_setFile(file, channels);
        }
        _save_and_relay(channels);
    }

    public final void split(final MidiTrack track) {
        final Set<Channel> channels = EnumSet.noneOf(Channel.class);
        final Map<Integer, List<MidiEvent>> extract = track.extractChannels();
        for (final List<MidiEvent> midiEvents : extract.values()) {
            final MidiTrack newTrack = core_create(channels);
            TrackUtil.add(newTrack, midiEvents);
        }
        channels.forEach(event -> audience.send(event, this));
    }

    private static class BACKUP_OVERFLOW extends IOException {
        public BACKUP_OVERFLOW() {
            super("Zu viele Backups!");
        }
    }

    private static class BACKUP_RENAME extends IOException {
        public BACKUP_RENAME(final String message) {
            super(message);
        }
    }

    private static class PART_MAP extends HashMap<javax.sound.midi.Track, MidiTrack> {
        private PART_MAP() {
        }
    }

    public final void onSetModified(final MidiTrack track) {
        if (track.isModified()) {
            final Set<Channel> channels = EnumSet.noneOf(Channel.class);
            core_setModified(true, channels);
            channels.forEach(event -> audience.send(event, this));
        }
    }
}
