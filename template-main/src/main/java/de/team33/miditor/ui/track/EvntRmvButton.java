package de.team33.miditor.ui.track;

import java.awt.event.ActionEvent;
import de.team33.midi.util.TrackUtil;

public abstract class EvntRmvButton extends EventButton {
    public EvntRmvButton() {
        super("del", 1);
    }

    public final void actionPerformed(ActionEvent e) {
        TrackUtil.remove(this.getTrackHandler().getTrack(), this.getTrackHandler().getTrackSelection());
        this.setSelected(false);
    }
}
