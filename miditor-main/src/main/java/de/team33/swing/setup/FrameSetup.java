//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.team33.swing.setup;

import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.Frames;

import java.awt.*;
import java.awt.event.ComponentEvent;

public abstract class FrameSetup {

    public final void init(final Frame frame) {
        Frames.builder();
        final Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
        frame.setSize(getSize(bounds.width * 4 / 5, bounds.height * 4 / 5));
        frame.setExtendedState(getExtendedState() & -2);
        frame.setLocationByPlatform(true);
        Event.COMPONENT_RESIZED.add(frame, this::onResized);
    }

    private void onResized(final ComponentEvent event) {
        final Frame frame = (Frame) event.getComponent();
        final int xState = frame.getExtendedState();
        if (xState != getExtendedState()) {
            setExtendedState(xState);
        } else if (xState == 0) {
            setSize(frame.getSize());
        }
    }

    protected abstract int getExtendedState();

    protected abstract void setExtendedState(int var1);

    protected abstract Dimension getSize(int width, int height);

    protected abstract void setSize(Dimension var1);
}
