package de.team33.miditor.backend.trials;

import de.team33.miditor.backend.MidiCenter;
import de.team33.miditor.backend.MidiPlayer;
import de.team33.miditor.backend.MidiPlayer.Trigger;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

class MidiCenterTrial {

    private final String[] args;
    private final MidiCenter midiCenter;

    private MidiCenterTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = new MidiCenter().load(Path.of("Beatles-Let_it_be-B.mid"));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new MidiCenterTrial(args).run();
    }

    private void run() throws InterruptedException {
        final MidiPlayer player = midiCenter.player();
        System.out.printf("Player state: %s%n", player.getState());

        player.push(Trigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(Trigger.PAUSE);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(2500);

        player.push(Trigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(Trigger.STOP);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(2500);

        player.push(Trigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(Trigger.OFF);
        System.out.printf("Player state: %s%n", player.getState());
        System.out.printf("- Quit - - - - - - -%n");
    }
}