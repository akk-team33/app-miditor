package de.team33.cmd.template.runtime;

import de.team33.patterns.io.deimos.TextIO;

@FunctionalInterface
public interface Output {

    @SuppressWarnings("Convert2MethodRef")
    Output SYSTEM = (format, args) -> System.out.printf(format, args);

    void printf(String format, Object... args);

    default Output printLines(final String text) {
        text.lines()
            .forEach(line -> printf("%s%n", line));
        printf("%n");
        return this;
    }

    default Output printHead() {
        printf("%s%n", TextIO.read(Output.class, "Head.txt"));
        return this;
    }

    default Output printHelp(final String text) {
        return printHead().printLines(text);
    }
}
