package de.team33.midi.trials;

import de.team33.midi.PieceOfMusic;
import de.team33.midi.Player;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

class PlayerTrial {

    private final String[] args;
    private final PieceOfMusic midiCenter;

    private PlayerTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = PieceOfMusic.loader(new SimpleAsyncExecutor())
                                      .load(Path.of("miditor-main/src/test/resources/de/team33/midi/testing/Jammin.mid"));
        this.midiCenter.registry()
                       .add(PieceOfMusic.Channel.SET_PATH,
                            path -> System.out.printf("Center path: %s%n", path));
        this.midiCenter.player().registry()
                       .add(Player.Channel.SET_STATE,
                            state -> System.out.printf("Player state: %s%n", state))
                       .add(Player.Channel.SET_STATE,
                            state -> System.out.printf("Effective triggers: %s%n", Player.Trigger.effectiveOn(state)))
                       .add(Player.Channel.SET_POSITION,
                            position -> System.out.printf("Player position: %s%n", position))
                       .add(Player.Channel.SET_TEMPO,
                            tempo -> System.out.printf("Player tempo: %s%n", tempo));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new PlayerTrial(args).run();
    }

    private void run() throws InterruptedException {
        final Player player = midiCenter.player();
        player.push(Player.Trigger.ON);

        player.push(Player.Trigger.START);
        Thread.sleep(5000);

        player.push(Player.Trigger.PAUSE);
        player.setPosition(player.getPosition() + 1);
        Thread.sleep(2500);

        player.push(Player.Trigger.START);
        Thread.sleep(5000);

        player.push(Player.Trigger.STOP);
        Thread.sleep(2500);

        player.setPosition(3000);
        player.push(Player.Trigger.START);
        Thread.sleep(5000);

        player.push(Player.Trigger.OFF);
        System.out.printf("- Quit - - - - - - -%n");
    }
}