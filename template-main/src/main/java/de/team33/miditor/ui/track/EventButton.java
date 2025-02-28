package de.team33.miditor.ui.track;

import de.team33.miditor.controller.UIController;
import de.team33.miditor.ui.SmallButton;

public abstract class EventButton extends SmallButton {
    private final int m_Min;
    private final int m_Max;

    public EventButton(final String text, final int min) {
        this(text, min, Integer.MAX_VALUE);
    }

    public EventButton(final String text, final int min, final int max) {
        super(text);
        m_Min = min;
        m_Max = max;
        getTrackHandler().addListener(UIController.Event.SetTrackSelection, this::onSetTrackSelection);
    }

    protected abstract UIController getTrackHandler();

    private void onSetTrackSelection(final UIController controller) {
        final int[] sel = controller.getTrackSelection();
        setEnabled((m_Min <= sel.length) && (sel.length <= m_Max));
    }
}
