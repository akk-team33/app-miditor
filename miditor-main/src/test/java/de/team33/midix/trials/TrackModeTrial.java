package de.team33.midix.trials;

import de.team33.midi.TrackMode;
import de.team33.midix.MidiCenter;
import de.team33.midix.MidiPlayer;
import de.team33.midix.PlayTrigger;
import de.team33.patterns.execution.metis.SimpleAsyncExecutor;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.out;

class TrackModeTrial {

    private final String[] args;
    private final MidiCenter midiCenter;

    private TrackModeTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = new MidiCenter(new SimpleAsyncExecutor()).load(Path.of("Beatles-Let_it_be-B.mid"));
        this.midiCenter.registry()
                       .add(MidiCenter.Channel.SET_PATH, path -> out.printf("Center path: %s%n", path));
        this.midiCenter.player().registry()
                       .add(MidiPlayer.Channel.SET_STATE, state -> out.printf("Player state: %s%n", state))
                       .add(MidiPlayer.Channel.SET_STATE, state -> out.printf("Effective triggers: %s%n",
                                                                              PlayTrigger.allEffectiveOn(state)))
                       .add(MidiPlayer.Channel.SET_TRACK_MODE, modes -> out.printf("Track modes: %s%n", modes));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new TrackModeTrial(args).run();
    }

    private void run() throws InterruptedException {
        final MidiPlayer player = midiCenter.player()
                                            .push(PlayTrigger.ON)
                                            .setPosition(30000)
                                            .push(PlayTrigger.START);
        Thread.sleep(5000);

        player.setTrackMode(3, TrackMode.MUTE);
        Thread.sleep(2000);

        player.setTrackMode(1, TrackMode.SOLO);
        Thread.sleep(1000);
        player.setTrackMode(7, TrackMode.SOLO);
        Thread.sleep(1000);
        player.setTrackMode(3, TrackMode.SOLO);
        Thread.sleep(1000);

        player.setTrackMode(1, TrackMode.NORMAL);
        player.setTrackMode(7, TrackMode.NORMAL);
        Thread.sleep(2000);

        player.setTrackMode(3, TrackMode.SOLO);
        Thread.sleep(2000);

        player.setTrackMode(3, TrackMode.MUTE);
        Thread.sleep(1000);

        player.setTrackMode(8, TrackMode.SOLO);
        player.setTrackMode(8, TrackMode.NORMAL);
        Thread.sleep(5000);

        player.push(PlayTrigger.OFF);
        out.printf("- Quit - - - - - - - - - - - - - - - - - - - - - - - - - - - -%n");
    }
}