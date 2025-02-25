package de.team33.miditor.visible;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.team33.messaging.Listener;
import net.team33.midi.Player;
import net.team33.midi.Sequence;
import net.team33.midi.Track;
import net.team33.midi.Player.State;
import net.team33.midi.impl.PlayerImpl;
import net.team33.miditor.controller.UIController;
import net.team33.miditor.model.PartSelection;
import net.team33.miditor.ui.sequence.Context;
import net.team33.selection.Selection;
import net.team33.swing.XFrame;

public class MainFrame extends XFrame {
    private static final String FRAME_TITLE = "%s - Miditor 01a/12";
    private static final Insets GBC_INSETS = new Insets(3, 3, 3, 3);
    private static final int GBC_ANCHOR = 10;
    private static final int GBC_FILL = 1;
    private static final GridBagConstraints GBC_FILE_CTRL;
    private static final GridBagConstraints GBC_ACTN_CTRL;
    private static final GridBagConstraints GBC_SPACE1;
    private static final GridBagConstraints GBC_TMPO_CTRL;
    private static final GridBagConstraints GBC_CTRL_PANE;
    private static final GridBagConstraints GBC_LCTR_PANE;
    private static final GridBagConstraints GBC_TRCK_LIST;
    private static final GridBagConstraints GBC_TRCK_EDIT;
    private final Selection<Track> selection;
    private final Sequence sequence;
    private final Player player;
    private final EventEditor m_EventEditor;
    private final WindowListener m_WindowListener = new WINDOW_ADAPTER();
    private final CONTEXT context = new CONTEXT();
    private final PLAY_CTRLS playCtrls = new PLAY_CTRLS();
    private final SONG_CTRLS songCtrls = new SONG_CTRLS();
    private final SONG_CLIENT m_SongClient = new SONG_CLIENT();

    static {
        GBC_FILE_CTRL = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_ACTN_CTRL = new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_SPACE1 = new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_TMPO_CTRL = new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_CTRL_PANE = new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_LCTR_PANE = new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_TRCK_LIST = new GridBagConstraints(0, 1, 4, 2, 1.0, 1.0, 10, 1, GBC_INSETS, 0, 0);
        GBC_TRCK_EDIT = new GridBagConstraints(4, 1, 2, 1, 0.0, 1.0, 10, 1, GBC_INSETS, 0, 0);
    }

    public MainFrame(Sequence sequence, Preferences prefs) throws MidiUnavailableException {
        super("?", prefs);
        this.sequence = sequence;
        this.selection = new PartSelection(this.sequence);
        this.player = new PlayerImpl(this.sequence);
        this.m_EventEditor = new TRACK_EDITOR();
        this.setIconImage(Rsrc.MAIN_ICON.getImage());
        this.setContentPane(new MAIN_PANE());
        this.setLocationByPlatform(true);
        this.addWindowListener(this.m_WindowListener);
        this.player.getSequence().getRegister(Sequence.SetFile.class).add(this.m_SongClient);
    }

    protected void finalize() throws Throwable {
        this.player.getSequence().getRegister(Sequence.SetFile.class).remove(this.m_SongClient);
        this.removeWindowListener(this.m_WindowListener);
        super.finalize();
    }

    private class CENTER_PANE extends JTabbedPane {
        public CENTER_PANE() {
            super(1);
            this.addTab("Track-Übersicht", (Icon)null, MainFrame.this.songCtrls.getTrackList(), "Übersicht über die im aktuellen Song enthaltenen 'Tonspuren' (Tracks)");
            this.addTab("Event-Editor", (Icon)null, MainFrame.this.m_EventEditor.getComponent(), "Event-Editor");
        }
    }

    private class CONTEXT implements Context, PlayerControls.Context {
        private CONTEXT() {
        }

        public Component getFrame() {
            return MainFrame.this;
        }

        public Player getPlayer() {
            return MainFrame.this.player;
        }

        public Selection<Track> getSelection() {
            return MainFrame.this.selection;
        }

        public Sequence getSequence() {
            return MainFrame.this.sequence;
        }

        public UIController getTrackHandler() {
            return MainFrame.this.m_EventEditor;
        }

        public Selection<Track> getTrackSelection() {
            return MainFrame.this.selection;
        }

        public Window getWindow() {
            return MainFrame.this;
        }
    }

    private class MAIN_PANE extends JPanel {
        public MAIN_PANE() {
            super(new BorderLayout());
            this.add(MainFrame.this.new NORTH_PANE(), "North");
            this.add(MainFrame.this.new CENTER_PANE(), "Center");
        }
    }

    private class NORTH_PANE extends JPanel {
        public NORTH_PANE() {
            super(new GridBagLayout());
            this.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));
            this.add(MainFrame.this.songCtrls.getFileControl(), MainFrame.GBC_FILE_CTRL);
            this.add(MainFrame.this.songCtrls.getActionControl(), MainFrame.GBC_ACTN_CTRL);
            this.add(new JPanel(), MainFrame.GBC_SPACE1);
            this.add(MainFrame.this.playCtrls.getTempoControl(), MainFrame.GBC_TMPO_CTRL);
            this.add(MainFrame.this.playCtrls.getDriveControl(), MainFrame.GBC_CTRL_PANE);
            this.add(MainFrame.this.playCtrls.getLocator(), MainFrame.GBC_LCTR_PANE);
        }
    }

    private class PLAY_CTRLS extends PlayerControls {
        private PLAY_CTRLS() {
        }

        protected PlayerControls.Context getRootContext() {
            return MainFrame.this.context;
        }
    }

    private class SONG_CLIENT implements Listener<Sequence.SetFile> {
        private SONG_CLIENT() {
        }

        public void pass(Sequence.SetFile message) {
            File f = ((Sequence)message.getSender()).getFile();
            MainFrame.this.setTitle(String.format("%s - Miditor 01a/12", f.getPath()));
        }
    }

    private class SONG_CTRLS extends SongControls {
        private SONG_CTRLS() {
        }

        protected Context getContext() {
            return MainFrame.this.context;
        }
    }

    private class TRACK_EDITOR extends EventEditor {
        private TRACK_EDITOR() {
        }

        protected Sequence getSequence() {
            return MainFrame.this.player.getSequence();
        }
    }

    private class WINDOW_ADAPTER extends WindowAdapter {
        private WINDOW_ADAPTER() {
        }

        public void windowClosed(WindowEvent e) {
            MainFrame.this.player.setState(State.IDLE);
        }

        public void windowOpened(WindowEvent e) {
            MainFrame.this.player.setState(State.STOP);
        }
    }
}
