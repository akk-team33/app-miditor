package de.team33.miditor.ui;

import de.team33.midi.Music;
import de.team33.midi.Part;
import de.team33.midi.Player;
import de.team33.midi.Score;
import de.team33.miditor.controller.UIController;
import de.team33.miditor.model.Selection;
import de.team33.miditor.ui.sequence.Context;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.sphinx.alpha.activity.Event;
import de.team33.sphinx.alpha.visual.JPanels;
import de.team33.sphinx.alpha.visual.JTabbedPanes;
import de.team33.swing.XFrame;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.prefs.Preferences;

public class MainFrame extends XFrame {
    private static final String FRAME_TITLE = TextIO.read(MainFrame.class, "MainFrameTitle.txt");
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

    private final Selection<Part> selection = new Selection<>();
    private final Music music;
    private final Factory factory;

    public MainFrame(final Music music, final Preferences prefs) {
        super(FRAME_TITLE, prefs);
        this.music = music;
        this.factory = new Factory();

        music.score().registry().add(Score.Channel.SetTracks, any -> selection.clear());

        setIconImage(Rsrc.MAIN_ICON.getImage());
        setContentPane(factory.mainPane());
        setLocationByPlatform(true);

        Event.WINDOW_OPENED.add(this, ignored -> music.player().push(Player.Trigger.ON));
        Event.WINDOW_CLOSED.add(this, ignored -> music.player().push(Player.Trigger.OFF));

        music.registry().add(Music.Channel.SET_PATH, this::onSetFile);
    }

    private final class Factory extends de.team33.miditor.ui.Factory implements Context {

        private final EventEditor eventEditor;
        private final PLAY_CTRLS playCtrls;
        private final SONG_CTRLS songCtrls;

        private Factory() {
            this.eventEditor = new EventEditor(score());
            this.playCtrls = new PLAY_CTRLS();
            this.songCtrls = new SONG_CTRLS();
        }

        public final Window window() {
            return MainFrame.this;
        }

        @Override
        public final Music music() {
            return music;
        }

        public final Selection<Part> selection() {
            return selection;
        }

        public final UIController partHandler() {
            return eventEditor;
        }

        private JPanel mainPane() {
            return JPanels.builder()
                          .setLayout(new BorderLayout())
                          .add(northPanel(), BorderLayout.NORTH)
                          .add(centerPanel(), BorderLayout.CENTER)
                          .build();
        }

        private JPanel northPanel() {
            return JPanels.builder()
                          .setLayout(new GridBagLayout())
                          .setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1))
                          .add(songCtrls.getFileControl(), GBC_FILE_CTRL)
                          .add(songCtrls.getActionControl(), GBC_ACTN_CTRL)
                          .add(new JPanel(), GBC_SPACE1)
                          .add(playCtrls.getTempoControl(), GBC_TMPO_CTRL)
                          .add(playCtrls.getDriveControl(), GBC_CTRL_PANE)
                          .add(playCtrls.getLocator(), GBC_LCTR_PANE)
                          .build();
        }

        private JTabbedPane centerPanel() {
            return JTabbedPanes.builder()
                               .setTabPlacement(JTabbedPane.TOP)
                               .addTab("Track-Übersicht", null, songCtrls.getTrackList(), "Übersicht über die im aktuellen Song enthaltenen 'Tonspuren' (Tracks)")
                               .addTab("Event-Editor", null, eventEditor.getComponent(), "Event-Editor")
                               .build();
        }
    }

    private class PLAY_CTRLS extends PlayerControls {

        @Override
        protected de.team33.miditor.ui.Context getRootContext() {
            return factory;
        }
    }

    public final void onSetFile(final Path path) {
        setTitle(String.format(FRAME_TITLE, path));
    }

    private class SONG_CTRLS extends SongControls {
        protected final Context getContext() {
            return factory;
        }
    }
}
