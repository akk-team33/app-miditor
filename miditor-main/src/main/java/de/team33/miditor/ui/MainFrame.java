package de.team33.miditor.ui;

import de.team33.midi.Part;
import de.team33.midi.PieceOfMusic;
import de.team33.midi.Player;
import de.team33.midi.Score;
import de.team33.midi.Timing;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.model.PartSelection;
import de.team33.miditor.ui.sequence.Context;
import de.team33.selection.Selection;
import de.team33.swing.XFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.file.Path;
import java.util.prefs.Preferences;

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

    private final Selection<Part> selection;
    private final PieceOfMusic music;
    private final EventEditor m_EventEditor;
    private final WindowListener m_WindowListener = new WINDOW_ADAPTER();
    private final CONTEXT context = new CONTEXT();
    private final PLAY_CTRLS playCtrls = new PLAY_CTRLS();
    private final SONG_CTRLS songCtrls = new SONG_CTRLS();

    public MainFrame(final PieceOfMusic music, final Preferences prefs) {
        super("?", prefs);
        this.music = music;
        selection = new PartSelection(music.score());
        m_EventEditor = new TRACK_EDITOR();
        setIconImage(Rsrc.MAIN_ICON.getImage());
        setContentPane(new MAIN_PANE());
        setLocationByPlatform(true);
        addWindowListener(m_WindowListener);
        music.registry().add(PieceOfMusic.Channel.SET_PATH, this::onSetFile);
    }

//    protected void finalize() throws Throwable {
//        player.getSequence().getRegister(Sequence.SetFile.class).remove(m_SongClient);
//        removeWindowListener(m_WindowListener);
//        super.finalize();
//    }

    private class CENTER_PANE extends JTabbedPane {
        CENTER_PANE() {
            super(1);
            addTab("Track-Übersicht", (Icon) null, songCtrls.getTrackList(), "Übersicht über die im aktuellen Song enthaltenen 'Tonspuren' (Tracks)");
            addTab("Event-Editor", (Icon) null, m_EventEditor.getComponent(), "Event-Editor");
        }
    }

    private class CONTEXT implements Context, PlayerControls.Context {

        public Component getFrame() {
            return MainFrame.this;
        }

        @Override
        public PieceOfMusic getMusic() {
            return music;
        }

        @Override
        public Timing getTiming() {
            return music.score().getTiming();
        }

        public Player getPlayer() {
            return music.player();
        }

        public Selection<Part> getSelection() {
            return selection;
        }

        public Score getSequence() {
            return music.score();
        }

        public UIController getTrackHandler() {
            return m_EventEditor;
        }

        public Selection<Part> getTrackSelection() {
            return selection;
        }

        public Window getWindow() {
            return MainFrame.this;
        }
    }

    private class MAIN_PANE extends JPanel {
        public MAIN_PANE() {
            super(new BorderLayout());
            add(MainFrame.this.new NORTH_PANE(), "North");
            add(MainFrame.this.new CENTER_PANE(), "Center");
        }
    }

    private class NORTH_PANE extends JPanel {
        public NORTH_PANE() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));
            add(songCtrls.getFileControl(), MainFrame.GBC_FILE_CTRL);
            add(songCtrls.getActionControl(), MainFrame.GBC_ACTN_CTRL);
            add(new JPanel(), MainFrame.GBC_SPACE1);
            add(playCtrls.getTempoControl(), MainFrame.GBC_TMPO_CTRL);
            add(playCtrls.getDriveControl(), MainFrame.GBC_CTRL_PANE);
            add(playCtrls.getLocator(), MainFrame.GBC_LCTR_PANE);
        }
    }

    private class PLAY_CTRLS extends PlayerControls {

        protected PlayerControls.Context getRootContext() {
            return context;
        }
    }

    public void onSetFile(final Path path) {
        setTitle(String.format("%s - Miditor 01a/12", path));
    }

    private class SONG_CTRLS extends SongControls {
        protected Context getContext() {
            return context;
        }
    }

    private class TRACK_EDITOR extends EventEditor {
        protected Score getSequence() {
            return music.score();
        }
    }

    private class WINDOW_ADAPTER extends WindowAdapter {

        public void windowClosed(final WindowEvent e) {
            music.player().push(Player.Trigger.OFF);
        }

        public void windowOpened(final WindowEvent e) {
            music.player().push(Player.Trigger.ON);
        }
    }
}
