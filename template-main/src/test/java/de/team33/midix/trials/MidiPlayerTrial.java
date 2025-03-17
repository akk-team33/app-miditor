package de.team33.midix.trials;

import de.team33.midix.MidiCenter;
import de.team33.midix.MidiPlayer;
import de.team33.midix.PlayTrigger;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

class MidiPlayerTrial {

    private final String[] args;
    private final MidiCenter midiCenter;

    private MidiPlayerTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = new MidiCenter().load(Path.of("template-main/src/test/resources/de/team33/midi/testing/Jammin.mid"));
        this.midiCenter.add(MidiCenter.Channel.SET_PATH,
                            path -> System.out.printf("Center path: %s%n", path));
        this.midiCenter.player().add(MidiPlayer.Channel.SET_STATE,
                                     state -> System.out.printf("Player state: %s%n", state));
        this.midiCenter.player().add(MidiPlayer.Channel.SET_STATE,
                                     state -> System.out.printf("Effective triggers: %s%n", PlayTrigger.allEffectiveOn(state)));
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
        player.push(PlayTrigger.ON);

        player.push(PlayTrigger.START);
        Thread.sleep(5000);

        player.push(PlayTrigger.PAUSE);
        player.setPosition(player.getPosition() + 1);
        Thread.sleep(2500);

        player.push(PlayTrigger.START);
        Thread.sleep(5000);

        player.push(PlayTrigger.STOP);
        Thread.sleep(2500);

        player.setPosition(3000);
        player.push(PlayTrigger.START);
        Thread.sleep(5000);

        player.push(PlayTrigger.OFF);
        System.out.printf("- Quit - - - - - - -%n");
    }
}