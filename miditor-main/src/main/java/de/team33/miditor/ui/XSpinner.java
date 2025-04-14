package de.team33.miditor.ui;

import de.team33.patterns.building.elara.LateBuilder;
import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.JLabels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class XSpinner extends JLabel {

    private final AtomicInteger value = new AtomicInteger();
    @SuppressWarnings("SpellCheckingInspection")
    private final AtomicBoolean hilited = new AtomicBoolean();
    @SuppressWarnings("FieldHasSetterButNoGetter")
    private final AtomicBoolean entered = new AtomicBoolean();
    @SuppressWarnings("FieldHasSetterButNoGetter")
    private final AtomicBoolean focussed = new AtomicBoolean();
    private boolean oldOpaque = isOpaque();
    private Color oldForeground = getForeground();
    private Color oldBackground = getBackground();

    public static Builder builder() {
        return new Builder().setCursor(new Cursor(Cursor.N_RESIZE_CURSOR))
                            .setFocusable(true)
                            .on(Event.MOUSE_ENTERED, cast((spinner, event) -> spinner.setEntered(true)))
                            .on(Event.MOUSE_EXITED, cast((spinner, event) -> spinner.setEntered(false)))
                            .on(Event.MOUSE_PRESSED, cast((spinner, event) -> spinner.requestFocus()))
                            //.on(Event.MOUSE_WHEEL_MOVED, XSpinner::onMouseWheelMoved)
                            .on(Event.FOCUS_GAINED, cast((spinner, event) -> spinner.setFocussed(true)))
                            .on(Event.FOCUS_LOST, cast((spinner, event) -> spinner.setFocussed(false)));
    }

    private static <E extends ComponentEvent> Consumer<E> cast(final BiConsumer<XSpinner, E> consumer) {
        return event -> {
            if (event.getComponent() instanceof final XSpinner spinner) {
                consumer.accept(spinner, event);
            } else {
                throw new IllegalStateException("unexpected event: " + event);
            }
        };
    }

    private void setEntered(final boolean entered) {
        this.entered.set(entered);
        setHilited();
    }

    private void setHilited() {
        final boolean newHilited = (entered.get() || focussed.get());
        if (newHilited  != hilited.get()) {
            hilited.set(newHilited);
            if (newHilited) {
                oldOpaque = isOpaque();
                oldForeground = getForeground();
                oldBackground = getBackground();
                setOpaque(true);
                setForeground(Color.BLACK);
                setBackground(Color.LIGHT_GRAY);
            } else {
                setOpaque(oldOpaque);
                setForeground(oldForeground);
                setBackground(oldBackground);
            }
        }
    }

    private void setFocussed(final boolean focussed) {
        this.focussed.set(focussed);
        setHilited();
    }

    public final int getValue() {
        return value.get();
    }

    public final void setValue(final int value) {
        this.value.set(value);
        setText(String.format("%03d", value));
    }

    @FunctionalInterface
    public interface Setup<S extends Setup<S>> extends JLabels.Setup<XSpinner, S> {

        default S setValue(final int value) {
            return setup(xSpinner -> xSpinner.setValue(value));
        }
    }

    public static final class Builder extends LateBuilder<XSpinner, Builder> implements Setup<Builder> {

        private Builder() {
            super(XSpinner::new, Builder.class);
        }
    }
}
