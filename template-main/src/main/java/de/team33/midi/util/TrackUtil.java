package de.team33.midi.util;

import de.team33.midi.Track;

import javax.sound.midi.MidiEvent;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class TrackUtil {
    public TrackUtil() {
    }

    public static void add(Track track, Collection<MidiEvent> events) {
        MidiEvent[] array = (MidiEvent[])events.toArray(new MidiEvent[events.size()]);
        track.add(array);
    }

    public static List<MidiEvent> getEvents(Track track, int[] selection) {
        List<MidiEvent> ret = new Vector(selection.length);
        int size = track.size();
        int[] var7 = selection;
        int var6 = selection.length;

        for(int var5 = 0; var5 < var6; ++var5) {
            int index = var7[var5];
            if (index >= 0 && index < size) {
                ret.add(track.get(index));
            }
        }

        return ret;
    }

    public static void remove(Track track, Collection<MidiEvent> events) {
        MidiEvent[] array = (MidiEvent[])events.toArray(new MidiEvent[events.size()]);
        track.remove(array);
    }

    public static void remove(Track track, int[] selection) {
        remove(track, (Collection)getEvents(track, selection));
    }
}
