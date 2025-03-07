package de.team33.miditor.backend.trials;

import de.team33.miditor.backend.MidiCenter;
import de.team33.miditor.backend.MidiPlayer;
import de.team33.miditor.backend.TrackMode;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;

import static de.team33.miditor.backend.MidiPlayer.Channel;
import static de.team33.miditor.backend.MidiPlayer.Trigger;
import static java.lang.System.out;

class TrackModeTrial {

    private final String[] args;
    private final MidiCenter midiCenter;

    private TrackModeTrial(final String[] args) throws InvalidMidiDataException, IOException {
        this.args = args;
        this.midiCenter = new MidiCenter().load(Path.of("Beatles-Let_it_be-B.mid"));
        this.midiCenter.add(MidiCenter.Channel.SET_PATH, path -> out.printf("Center path: %s%n", path))
                       .player()
                       .add(Channel.SET_STATE, state -> out.printf("Player state: %s%n", state))
                       .add(Channel.SET_STATE, state -> out.printf("Effective triggers: %s%n",
                                                                   Trigger.allEffectiveOn(state)))
                       .add(Channel.SET_TRACK_MODE, modes -> out.printf("Track modes: %s%n", modes));
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static void main(final String[] args) throws Exception {
        new TrackModeTrial(args).run();
    }

    private void run() throws InterruptedException {
        final MidiPlayer player = midiCenter.player()
                                            .push(Trigger.ON)
                                            .setPosition(30000)
                                            .push(Trigger.START);
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

        player.push(Trigger.OFF);
        out.printf("- Quit - - - - - - - - - - - - - - - - - - - - - - - - - - - -%n");
    }
}