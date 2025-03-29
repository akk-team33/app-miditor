package de.team33.midi.trials;

import de.team33.midi.Music;
import de.team33.midi.Player;
import de.team33.midi.TrackMode;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.out;

class TrackModeTrial {

    private final String[] args;
    private final Music midiCenter;

    private TrackModeTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = Music.loader(new SimpleAsyncExecutor())
                               .load(Path.of("miditor-main/src/test/resources/de/team33/midi/testing/Respect.mid"));
        this.midiCenter.registry()
                       .add(Music.Channel.SET_PATH, path -> out.printf("Center path: %s%n", path));
        this.midiCenter.player().registry()
                       .add(Player.Channel.SET_STATE, state -> out.printf("Player state: %s%n", state))
                       .add(Player.Channel.SET_STATE, state -> out.printf("Effective triggers: %s%n",
                                                                              Player.Trigger.effectiveOn(state)))
                       .add(Player.Channel.SET_MODES, modes -> out.printf("Track modes: %s%n", modes));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new TrackModeTrial(args).run();
    }

    private void run() throws InterruptedException {
        final Player player = midiCenter.player();
        player.push(Player.Trigger.ON);
        player.setPosition(30000);
        player.push(Player.Trigger.START);
        Thread.sleep(5000);

        player.setMode(3, TrackMode.MUTE);
        Thread.sleep(2000);

        player.setMode(1, TrackMode.SOLO);
        Thread.sleep(1000);
        player.setMode(7, TrackMode.SOLO);
        Thread.sleep(1000);
        player.setMode(3, TrackMode.SOLO);
        Thread.sleep(1000);

        player.setMode(1, TrackMode.NORMAL);
        player.setMode(7, TrackMode.NORMAL);
        Thread.sleep(2000);

        player.setMode(3, TrackMode.SOLO);
        Thread.sleep(2000);

        player.setMode(3, TrackMode.MUTE);
        Thread.sleep(1000);

        player.setMode(8, TrackMode.SOLO);
        player.setMode(8, TrackMode.NORMAL);
        Thread.sleep(5000);

        player.push(Player.Trigger.OFF);
        out.printf("- Quit - - - - - - - - - - - - - - - - - - - - - - - - - - - -%n");
    }
}