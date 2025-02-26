package de.team33.miditor.model;

import net.team33.messaging.Listener;
import net.team33.midi.Sequence;
import net.team33.midi.Track;
import net.team33.selection.SelectionImpl;

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
