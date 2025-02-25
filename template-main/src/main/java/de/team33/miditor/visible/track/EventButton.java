package de.team33.miditor.visible.track;

import net.team33.messaging.Listener;
import net.team33.miditor.controller.UIController;
import net.team33.miditor.ui.SmallButton;

public abstract class EventButton extends SmallButton {
    private final int m_Min;
    private final int m_Max;

    public EventButton(String text, int min) {
        this(text, min, Integer.MAX_VALUE);
    }

    public EventButton(String text, int min, int max) {
        super(text);
        this.m_Min = min;
        this.m_Max = max;
        this.getTrackHandler().getRegister(UIController.SetTrackSelection.class).add(new TH_CLIENT());
    }

    protected abstract UIController getTrackHandler();

    private class TH_CLIENT implements Listener<UIController.SetTrackSelection> {
        private TH_CLIENT() {
        }

        public void pass(UIController.SetTrackSelection message) {
            int[] sel = message.getSender().getTrackSelection();
            EventButton.this.setEnabled(EventButton.this.m_Min <= sel.length && sel.length <= EventButton.this.m_Max);
        }
    }
}
