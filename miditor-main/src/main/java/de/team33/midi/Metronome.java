package de.team33.midi;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

import static de.team33.midi.Util.Message.Type.NOTE_ON;
import static de.team33.midi.Util.MetaMessage.Type.TRACK_NAME;

public class Metronome extends AbstractList<MidiEvent> {

    private final List<MidiEvent> backing = new LinkedList<>();

    public Metronome(final Parameter p) {
        final MetaMessage trackName =
                TRACK_NAME.newMessage("Metronome".getBytes(StandardCharsets.UTF_8));
        backing.add(new MidiEvent(trackName, 0L));
        for (long pos = p.getMin(); pos <= p.getMax(); pos += p.getRes()) {
            final ShortMessage noteOn =
                    NOTE_ON.newChnMessage(p.getChannel(), p.getNoteNo(pos), p.getDynamic(pos));
            final ShortMessage noteOff =
                    NOTE_ON.newChnMessage(p.getChannel(), p.getNoteNo(pos), 0);
            backing.add(new MidiEvent(noteOn, pos));
            backing.add(new MidiEvent(noteOff, pos + ((long) p.getRes() / 4)));
        }
    }

    @Override
    public final MidiEvent get(final int index) {
        return backing.get(index);
    }

    @Override
    public final int size() {
        return backing.size();
    }

    public interface Parameter {

        long getMin();

        long getMax();

        int getRes();

        int getChannel();

        int getNoteNo(long var1);

        int getDynamic(long var1);
    }
}
