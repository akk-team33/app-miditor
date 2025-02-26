package de.team33.miditor.controller;

import de.team33.messaging.Register;
import de.team33.messaging.sync.Router;
import de.team33.midi.Track;

import java.util.Arrays;

public class UIControllerImpl implements UIController {
    private final MESSAGE msgSetTrack = new SET_TRACK();
    private final MESSAGE msgUnsetTrack = new UNSET_TRACK();
    private final MESSAGE msgSetSelection = new SET_TRACK_SELECTION();
    private final ROUTER router = new ROUTER();
    private int[] m_Selection = new int[0];
    private Track m_Track = null;

    public UIControllerImpl() {
        this.router.addInitials(Arrays.asList(this.msgSetTrack, this.msgSetSelection));
    }

    public <MSX extends UIController.Message> Register<MSX> getRegister(Class<MSX> msgClass) {
        return this.router.getRegister(msgClass);
    }

    public Track getTrack() {
        return this.m_Track;
    }

    public void setTrack(Track track) {
        this.router.pass(this.msgUnsetTrack);
        this.m_Track = track;
        this.router.pass(this.msgSetTrack);
    }

    public int[] getTrackSelection() {
        return this.m_Selection;
    }

    public void setTrackSelection(int[] newSelection) {
        if (!Arrays.equals(this.m_Selection, newSelection)) {
            this.m_Selection = newSelection;
            this.router.pass(this.msgSetSelection);
        }

    }

    private static class ROUTER extends Router<UIController.Message> {
        private ROUTER() {
        }
    }

    private class MESSAGE implements UIController.Message {
        private MESSAGE() {
        }

        public UIController getSender() {
            return UIControllerImpl.this;
        }
    }

    private class SET_TRACK extends MESSAGE implements UIController.SetTrack {
        private SET_TRACK() {
            super();
        }
    }

    private class SET_TRACK_SELECTION extends MESSAGE implements UIController.SetTrackSelection {
        private SET_TRACK_SELECTION() {
            super();
        }
    }

    private class UNSET_TRACK extends MESSAGE implements UIController.UnsetTrack {
        private UNSET_TRACK() {
            super();
        }
    }
}
