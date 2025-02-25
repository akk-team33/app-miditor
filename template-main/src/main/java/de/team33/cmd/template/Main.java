package de.team33.cmd.template;

import de.team33.cmd.template.job.Regular;
import de.team33.cmd.template.runtime.BadConditionException;
import de.team33.cmd.template.runtime.Condition;
import de.team33.cmd.template.runtime.Output;

public class Main {

    public static void main(final String... args) {
        job(args).run();
    }

    private static Runnable job(final String... args) {
        final Output out = Output.SYSTEM;
        try {
            return Regular.job(Condition.of(out, args));
        } catch (final BadConditionException e) {
            return () -> out.printHelp(e.getMessage());
        }
    }
}
