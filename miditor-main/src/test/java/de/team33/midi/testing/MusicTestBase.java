package de.team33.midi.testing;

import de.team33.testing.io.hydra.FileIO;

import java.nio.file.Path;

public class MusicTestBase {

    private static final String RESPECT_MID_NAME = "Respect.mid";
    private static final Path TEST_PATH = Path.of("target", "testing", "music");
    private static final Path RESPECT_MID = TEST_PATH.resolve(RESPECT_MID_NAME);

    protected MusicTestBase() {
        FileIO.copy(MusicTestBase.class, RESPECT_MID_NAME, RESPECT_MID);
    }

    protected final Path path() {
        return RESPECT_MID;
    }
}