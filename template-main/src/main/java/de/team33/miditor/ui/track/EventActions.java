package de.team33.miditor.ui.track;

import java.awt.Component;
import javax.swing.JPanel;
import net.team33.miditor.controller.UIController;

public abstract class EventActions {
    private Component m_MainPane = null;

    public EventActions() {
    }

    public Component getComponent() {
        if (this.m_MainPane == null) {
            this.m_MainPane = new MAIN_PANE();
        }

        return this.m_MainPane;
    }

    protected abstract UIController getTrackHandler();

    private EventActions _EventActions() {
        return this;
    }

    private class DEL_BTTN extends EvntRmvButton {
        private DEL_BTTN() {
        }

        protected UIController getTrackHandler() {
            return EventActions.this._EventActions().getTrackHandler();
        }
    }

    private class MAIN_PANE extends JPanel {
        MAIN_PANE() {
            this.add(EventActions.this.new DEL_BTTN());
        }
    }
}
