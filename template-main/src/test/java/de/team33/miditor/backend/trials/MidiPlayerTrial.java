package de.team33.miditor.backend.trials;

import de.team33.miditor.backend.MidiCenter;
import de.team33.miditor.backend.MidiPlayer;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

class MidiPlayerTrial {

    private final String[] args;
    private final MidiCenter midiCenter;

    private MidiPlayerTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = new MidiCenter().load(Path.of("Beatles-Let_it_be-B.mid"));
        this.midiCenter.add(MidiCenter.Channel.SET_PATH,
                            path -> System.out.printf("Center path: %s%n", path));
        this.midiCenter.player().add(MidiPlayer.Channel.SET_STATE,
                                     state -> System.out.printf("Player state: %s%n", state));
        this.midiCenter.player().add(MidiPlayer.Channel.SET_POSITION,
                                     position -> System.out.printf("Player position: %s%n", position));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new MidiPlayerTrial(args).run();
    }

    private void run() throws InterruptedException {
        final MidiPlayer player = midiCenter.player();

        player.start();
        Thread.sleep(5000);

        player.pause();
        Thread.sleep(2500);

        player.start();
        Thread.sleep(5000);

        player.stop();
        Thread.sleep(2500);

        player.start();
        Thread.sleep(5000);

        player.off();
        System.out.printf("- Quit - - - - - - -%n");
    }
}