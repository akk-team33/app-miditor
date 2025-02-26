package de.team33.miditor.model;

import de.team33.midi.Sequence;
import de.team33.midi.Track;
import de.team33.selection.SelectionImpl;

public class PartSelection extends SelectionImpl<Track> {
    public PartSelection(Sequence sequence) {
        sequence.getRegister(Sequence.SetParts.class).add(new Consumer());
    }

    private class Consumer implements java.util.function.Consumer<Sequence.SetParts> {
        private Consumer() {
        }

        public void accept(Sequence.SetParts message) {
            PartSelection.this.clear();
        }
    }
}
