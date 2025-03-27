package de.team33.midix.trials;

import de.team33.midix.MidiCenter;
import de.team33.midix.MidiPlayer;
import de.team33.midix.PlayTrigger;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

class MidiCenterTrial {

    private final String[] args;
    private final MidiCenter midiCenter;

    private MidiCenterTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = new MidiCenter(new SimpleAsyncExecutor()).load(Path.of("miditor-main/src/test/resources/de/team33/midi/testing/Respect.mid"));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new MidiCenterTrial(args).run();
    }

    private void run() throws InterruptedException {
        final MidiPlayer player = midiCenter.player();
        System.out.printf("Player state: %s%n", player.getState());

        player.push(PlayTrigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(PlayTrigger.PAUSE);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(2500);

        player.push(PlayTrigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(PlayTrigger.STOP);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(2500);

        player.push(PlayTrigger.START);
        System.out.printf("Player state: %s%n", player.getState());
        Thread.sleep(5000);

        player.push(PlayTrigger.OFF);
        System.out.printf("Player state: %s%n", player.getState());
        System.out.printf("- Quit - - - - - - -%n");
    }
}