package de.team33.midi;

import de.team33.midi.testing.MusicTestBase;
import de.team33.patterns.mutable.alpha.Mutable;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceOfMusicTest extends MusicTestBase {

    private final PieceOfMusic music;

    PieceOfMusicTest() throws InvalidMidiDataException, IOException {
        this.music = PieceOfMusic.loader(Runnable::run)
                                 .load(path());
    }

    @Test
    final void setPath() {
        final Path expected = path().toAbsolutePath().normalize();
        final Mutable<Path> pathMutable = new Mutable<>(null);
        music.registry().add(PieceOfMusic.Channel.SET_PATH, pathMutable::set);

        assertEquals(expected, music.path());
        assertEquals(music.path(), pathMutable.get());

        music.setPath(Path.of(UUID.randomUUID().toString()));
        assertTrue(music.path().isAbsolute());
        assertNotEquals(expected, music.path());
        assertEquals(music.path(), pathMutable.get());
    }
}