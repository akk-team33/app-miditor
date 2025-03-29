//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.team33.swing.setup;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public abstract class FrameSetup {
    public FrameSetup() {
    }

    public final void init(Frame frame) {
        Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
        frame.setSize(this.getSize(bounds.width * 4 / 5, bounds.height * 4 / 5));
        frame.setExtendedState(this.getExtendedState() & -2);
        frame.setLocationByPlatform(true);
        frame.addComponentListener(new LISTENER());
    }

    protected abstract int getExtendedState();

    protected abstract void setExtendedState(int var1);

    protected abstract Dimension getSize(int var1, int var2);

    protected abstract void setSize(Dimension var1);

    private class LISTENER extends ComponentAdapter {
        private LISTENER() {
        }

        public final void componentResized(ComponentEvent e) {
            Frame frame = (Frame) e.getComponent();
            int xState = frame.getExtendedState();
            if (xState != FrameSetup.this.getExtendedState()) {
                FrameSetup.this.setExtendedState(xState);
            } else if (xState == 0) {
                FrameSetup.this.setSize(frame.getSize());
            }

        }
    }
}
