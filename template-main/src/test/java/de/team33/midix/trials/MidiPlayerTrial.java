package de.team33.midix.trials;

import de.team33.midix.MidiCenter;
import de.team33.midix.MidiPlayer;

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
        this.midiCenter.player().add(MidiPlayer.Channel.SET_STATE,
                                     state -> System.out.printf("Effective triggers: %s%n", MidiPlayer.Trigger.allEffectiveOn(state)));
        this.midiCenter.player().add(MidiPlayer.Channel.SET_POSITION,
                                     position -> System.out.printf("Player position: %s%n", position));
        this.midiCenter.player().add(MidiPlayer.Channel.SET_TEMPO,
                                     tempo -> System.out.printf("Player tempo: %s%n", tempo));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new MidiPlayerTrial(args).run();
    }

    private void run() throws InterruptedException {
        final MidiPlayer player = midiCenter.player();
        player.push(MidiPlayer.Trigger.ON);

        player.push(MidiPlayer.Trigger.START);
        Thread.sleep(5000);

        player.push(MidiPlayer.Trigger.PAUSE);
        player.setPosition(player.getPosition() + 1);
        Thread.sleep(2500);

        player.push(MidiPlayer.Trigger.START);
        Thread.sleep(5000);

        player.push(MidiPlayer.Trigger.STOP);
        Thread.sleep(2500);

        player.setPosition(3000);
        player.push(MidiPlayer.Trigger.START);
        Thread.sleep(5000);

        player.push(MidiPlayer.Trigger.OFF);
        System.out.printf("- Quit - - - - - - -%n");
    }
}