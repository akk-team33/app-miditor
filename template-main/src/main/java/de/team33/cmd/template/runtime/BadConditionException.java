package de.team33.cmd.template.runtime;

import de.team33.patterns.io.deimos.TextIO;

public class BadConditionException extends Exception {

    public BadConditionException(final String message) {
        super(message);
    }

    private static String message(final Class<?> referringClass) {
        return TextIO.read(referringClass, referringClass.getSimpleName() + ".txt");
    }

    public static BadConditionException read(final Class<?> referringClass) {
        return new BadConditionException(message(referringClass));
    }

    public static BadConditionException format(final Class<?> referringClass, final Object ... args) {
        return new BadConditionException(message(referringClass).formatted(args));
    }
}
