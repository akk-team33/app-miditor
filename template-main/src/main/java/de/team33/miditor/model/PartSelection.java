package de.team33.miditor.model;

import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.selection.SelectionImpl;
import net.team33.messaging.Listener;

public class PartSelection extends SelectionImpl<Track> {
    public PartSelection(Sequence sequence) {
        sequence.getRegister(Sequence.SetParts.class).add(new LISTENER());
    }

    private class LISTENER implements Listener<Sequence.SetParts> {
        private LISTENER() {
        }

        public void pass(Sequence.SetParts message) {
            PartSelection.this.clear();
        }
    }
}
