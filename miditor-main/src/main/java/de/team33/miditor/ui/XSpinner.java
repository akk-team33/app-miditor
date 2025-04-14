package de.team33.miditor.ui;

import de.team33.patterns.building.elara.LateBuilder;
import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.JLabels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class XSpinner extends JLabel {

    private final String format;
    private final List<Consumer<Integer>> audience = new LinkedList<>();
    private final AtomicInteger value = new AtomicInteger();
    @SuppressWarnings("SpellCheckingInspection")
    private final AtomicBoolean hilited = new AtomicBoolean();
    @SuppressWarnings("FieldHasSetterButNoGetter")
    private final AtomicBoolean entered = new AtomicBoolean();
    private final AtomicBoolean focussed = new AtomicBoolean();
    private boolean oldOpaque = isOpaque();
    private Color oldForeground = getForeground();
    private Color oldBackground = getBackground();

    private XSpinner(final String format) {
        this.format = format;
    }

    public static Builder builder(final String format) {
        return new Builder(format).setCursor(new Cursor(Cursor.N_RESIZE_CURSOR))
                                  .setFocusable(true)
                                  .on(Event.MOUSE_ENTERED, cast(XSpinner::onMouseEntered))
                                  .on(Event.MOUSE_EXITED, cast(XSpinner::onMouseExited))
                                  .on(Event.MOUSE_PRESSED, cast(XSpinner::onMousePressed))
                                  .on(Event.MOUSE_WHEEL_MOVED, cast(XSpinner::onMouseWheelMoved))
                                  .on(Event.KEY_PRESSED, cast(XSpinner::onKeyPressed))
                                  .on(Event.FOCUS_GAINED, cast(XSpinner::onFocusGained))
                                  .on(Event.FOCUS_LOST, cast(XSpinner::onFocusLost));
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

    @SuppressWarnings("MagicNumber")
    private void onKeyPressed(final KeyEvent event) {
        // System.out.printf("%d%n", event.getKeyCode());
        switch (event.getKeyCode()) {
        case 38, 39, 107, 521 -> setValue(getValue() + 1);
        case 37, 40, 45, 109 -> setValue(getValue() - 1);
        }
    }

    private void onFocusLost(final FocusEvent event) {
        focussed.set(false);
        setHilited();
    }

    private void onFocusGained(final FocusEvent event) {
        focussed.set(true);
        setHilited();
    }

    private void onMouseWheelMoved(final MouseWheelEvent event) {
        setValue(getValue() - event.getWheelRotation());
    }

    private void onMousePressed(final MouseEvent event) {
        requestFocus();
    }

    private void onMouseExited(final MouseEvent event) {
        setEntered(false);
    }

    private void onMouseEntered(final ComponentEvent event) {
        setEntered(true);
    }

    private void setEntered(final boolean entered) {
        this.entered.set(entered);
        setHilited();
    }

    private void setHilited() {
        final boolean newHilited = (entered.get() || focussed.get());
        if (newHilited != hilited.get()) {
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

    @SuppressWarnings("WeakerAccess")
    public final int getValue() {
        return value.get();
    }

    public final void setValue(final int value) {
        final int oldValue = getValue();
        if (value != oldValue && this.value.compareAndSet(oldValue, value)) {
            setText(String.format(format, value));
            audience.forEach(listener -> listener.accept(value));
        }
    }

    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface Setup<S extends Setup<S>> extends JLabels.Setup<XSpinner, S> {

        default S setValue(final int value) {
            return setup(xSpinner -> xSpinner.setValue(value));
        }

        default S onSetValue(final Consumer<Integer> listener) {
            return setup(xSpinner -> xSpinner.audience.add(listener));
        }
    }

    public static final class Builder extends LateBuilder<XSpinner, Builder> implements Setup<Builder> {

        private Builder(final String format) {
            super(() -> new XSpinner(format), Builder.class);
        }
    }
}
