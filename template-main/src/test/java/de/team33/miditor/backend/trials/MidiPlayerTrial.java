package de.team33.miditor.backend.trials;

import de.team33.miditor.backend.MidiCenter;
import de.team33.miditor.backend.MidiPlayer;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

import static de.team33.miditor.backend.MidiPlayer.*;

class MidiPlayerTrial {

    private final String[] args;
    private final MidiCenter midiCenter;

    private MidiPlayerTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = new MidiCenter().load(Path.of("Beatles-Let_it_be-B.mid"));
        this.midiCenter.add(MidiCenter.Channel.SET_PATH,
                            path -> System.out.printf("Center path: %s%n", path));
        this.midiCenter.player().add(Channel.SET_STATE,
                                     state -> System.out.printf("Player state: %s%n", state));
        this.midiCenter.player().add(Channel.SET_STATE,
                                     state -> System.out.printf("Effective triggers: %s%n", Trigger.allEffectiveOn(state)));
        this.midiCenter.player().add(Channel.SET_POSITION,
                                     position -> System.out.printf("Player position: %s%n", position));
        this.midiCenter.player().add(Channel.SET_TEMPO,
                                     tempo -> System.out.printf("Player tempo: %s%n", tempo));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new MidiPlayerTrial(args).run();
    }

    private void run() throws InterruptedException {
        final MidiPlayer player = midiCenter.player();
        player.act(Trigger.ON);

        player.act(Trigger.START);
        Thread.sleep(5000);

        player.act(Trigger.PAUSE);
        player.setPosition(player.position() + 1);
        Thread.sleep(2500);

        player.act(Trigger.START);
        Thread.sleep(5000);

        player.act(Trigger.STOP);
        Thread.sleep(2500);

        player.setPosition(3000);
        player.act(Trigger.START);
        Thread.sleep(5000);

        player.act(Trigger.OFF);
        System.out.printf("- Quit - - - - - - -%n");
    }
}