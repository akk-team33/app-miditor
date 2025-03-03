package de.team33.miditor.backend;

import de.team33.patterns.exceptional.dione.Converter;
import de.team33.patterns.exceptional.dione.Wrapping;

final class Util {

    static final Converter CNV = Converter.using(Wrapping.method(IllegalStateException::new));

    private Util() {
    }
}
