package de.team33.miditor.backend;

import de.team33.patterns.exceptional.dione.Converter;
import de.team33.patterns.exceptional.dione.Wrapping;

final class Util {

    static final Converter CNV = Converter.using(Wrapping.method(IllegalStateException::new));

    private Util() {
    }

    static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
