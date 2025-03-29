package de.team33.midi.trials;

import de.team33.midi.PieceOfMusic;
import de.team33.midi.Player;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
class PieceOfMusicTrial {

    private final String[] args;
    private final PieceOfMusic music;

    private PieceOfMusicTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.music = PieceOfMusic.loader(new SimpleAsyncExecutor())
                                 .load(Path.of("miditor-main/src/test/resources/de/team33/midi/testing/Respect.mid"));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new PieceOfMusicTrial(args).run();
    }

    private void run() throws InterruptedException {
        final Player player = music.player();
        System.out.printf("Player state: %s%n", player.getState());

        player.push(Player.Trigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(Player.Trigger.PAUSE);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(2500);

        player.push(Player.Trigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(Player.Trigger.STOP);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(2500);

        player.push(Player.Trigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(Player.Trigger.OFF);
        System.out.printf("Player state: %s%n", player.getState());
        System.out.printf("- Quit - - - - - - -%n");
    }
}