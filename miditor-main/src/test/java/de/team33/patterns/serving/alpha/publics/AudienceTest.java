package de.team33.patterns.serving.alpha.publics;

import de.team33.patterns.serving.alpha.Audience;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudienceTest {

    private static final String ANY_STRING = UUID.randomUUID().toString();

    @Test
    final void fire() {
        final List<String> target = new LinkedList<>();
        final Audience<String> audience = new Audience<>(Runnable::run);

        final int id = audience.subscribe(target::add);
        audience.fire(ANY_STRING);

        assertEquals(1, target.size());
        assertEquals(ANY_STRING, target.get(0));

        target.clear();
        audience.unsubscribe(id);
        audience.fire(ANY_STRING);

        assertEquals(0, target.size());
    }
}