package de.team33.miditor.ui.track;

import de.team33.midi.util.TrackUtil;

import java.awt.event.ActionEvent;

public abstract class EvntRmvButton extends EventButton {
    public EvntRmvButton() {
        super("del", 1);
    }

    public final void actionPerformed(final ActionEvent e) {
        TrackUtil.remove(this.getTrackHandler().getTrack(), this.getTrackHandler().getTrackSelection());
        this.setSelected(false);
    }
}
