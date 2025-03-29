package de.team33.midi;

import de.team33.midi.testing.MusicTestBase;
import de.team33.patterns.mutable.alpha.Mutable;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceOfMusicTest extends MusicTestBase {

    private final PieceOfMusic music;

    PieceOfMusicTest() throws InvalidMidiDataException, IOException {
        this.music = PieceOfMusic.loader(Runnable::run)
                                 .load(path());
    }

    @Test
    final void load() throws Exception {
        final PieceOfMusic other = PieceOfMusic.loader(Runnable::run)
                                               .load(music.path());
        assertEquals(music.score().getTracks().size(), other.score().getTracks().size());
    }

    @Test
    final void save() throws IOException {
        final long oldSize = Files.size(music.path());
        music.score().delete(List.of(music.score().getTracks().get(1)));

        music.save();
        final long newSize = Files.size(music.path());

        assertTrue(oldSize > newSize);
    }

    @Test
    final void saveAs() throws IOException {
        final Path oldPath = music.path();
        final Path newPath = oldPath.getParent().resolve(UUID.randomUUID() + ".mid");
        assertFalse(Files.exists(newPath, LinkOption.NOFOLLOW_LINKS));

        music.saveAs(newPath);
        assertTrue(Files.exists(newPath, LinkOption.NOFOLLOW_LINKS));
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