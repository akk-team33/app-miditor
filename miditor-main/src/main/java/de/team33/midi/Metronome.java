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

    public Metronome(final Parameter prm) {
        final MetaMessage trackName =
                TRACK_NAME.newMessage("Metronome".getBytes(StandardCharsets.UTF_8));
        backing.add(new MidiEvent(trackName, 0L));

        final int beatTicks = prm.timing().beatTicks();
        for (long pos = prm.startTick(); pos <= prm.finalTick(); pos += beatTicks) {
            final boolean first = (pos % prm.timing().barTicks()) == 0L;
            final int noteNo = first ? prm.firstNoteNo() : prm.nextNoteNo();
            final int dynamic = first ? prm.firstDynamic() : prm.nextDynamic();
            final ShortMessage noteOn =
                    NOTE_ON.newChnMessage(prm.midiChannel(), noteNo, dynamic);
            final ShortMessage noteOff =
                    NOTE_ON.newChnMessage(prm.midiChannel(), noteNo, 0);
            backing.add(new MidiEvent(noteOn, pos));
            backing.add(new MidiEvent(noteOff, pos + ((long) beatTicks / 4)));
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

    public record Parameter(Timing timing,
                            long startTick,
                            long finalTick,
                            int midiChannel,
                            int firstNoteNo,
                            int nextNoteNo,
                            int firstDynamic,
                            int nextDynamic) {}
}
