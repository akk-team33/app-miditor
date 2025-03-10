package de.team33.patterns.features.alpha.publics;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeaturesTest {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final Features features;
    private final Instant initialTime;
    private final int initialCount;

    FeaturesTest() {
        this.features = new Features();
        this.initialTime = features.get(Key.SELF_INIT_TIME);
        this.initialCount = features.get(Key.COUNTER);
    }

    @Test
    final void get() {
        assertEquals(hashCode(), features.get(Key.HASH_CODE));
        assertEquals(getClass().getCanonicalName(), features.get(Key.CLASS_NAME));
        assertEquals(initialTime, features.get(Key.SELF_INIT_TIME));
        assertEquals(initialCount, features.get(Key.COUNTER));
    }

    @Test
    final void reset_key() {
        assertEquals(initialTime, features.get(Key.SELF_INIT_TIME));
        assertEquals(initialCount, features.get(Key.COUNTER));

        features.reset(Key.SELF_INIT_TIME);

        final Instant newTime = features.get(Key.SELF_INIT_TIME);
        assertTrue(newTime.isAfter(initialTime),
                   () -> "<newTime> is expected to be after <%s> - but was <%s>".formatted(initialTime, newTime));
        assertEquals(initialCount, features.get(Key.COUNTER));
    }

    @Test
    final void reset_all() {
        assertEquals(initialTime, features.get(Key.SELF_INIT_TIME));
        features.reset();
        final Instant newTime = features.get(Key.SELF_INIT_TIME);
        assertTrue(newTime.isAfter(initialTime),
                   () -> "<newTime> is expected to be after <%s> - but was <%s>".formatted(initialTime, newTime));
        assertTrue(initialCount < features.get(Key.COUNTER));
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    private interface Key<R> extends de.team33.patterns.features.alpha.Features.Key<FeaturesTest, R> {

        Key<Integer> HASH_CODE = Object::hashCode;
        Key<String> CLASS_NAME = host -> host.getClass().getCanonicalName();
        Key<Instant> SELF_INIT_TIME = host -> Instant.now();
        Key<Integer> COUNTER = host -> FeaturesTest.COUNTER.getAndIncrement();
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    private final class Features extends de.team33.patterns.features.alpha.Features<FeaturesTest> {

        Features() {
            super(ConcurrentHashMap::new);
        }

        @Override
        protected FeaturesTest host() {
            return FeaturesTest.this;
        }
    }
}