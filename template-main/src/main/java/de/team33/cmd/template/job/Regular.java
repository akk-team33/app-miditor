package de.team33.cmd.template.job;

import de.team33.cmd.template.runtime.BadConditionException;
import de.team33.cmd.template.runtime.Condition;
import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.exceptional.dione.XFunction;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public enum Regular {

    ABOUT(About::job, About.EXCERPT),
    INFO(Info::job, Info.EXCERPT);

    private static final Values<Regular> VALUES = Values.of(Regular.class);

    private final XFunction<Condition, Runnable, BadConditionException> toJob;
    private final String excerpt;

    Regular(final XFunction<Condition, Runnable, BadConditionException> toJob, final String excerpt) {
        this.toJob = toJob;
        this.excerpt = excerpt;
    }

    public static String excerpts() {
        final int maxLength = VALUES.mapAll(value -> value.name().length())
                                    .reduce(0, Math::max);
        final String format = String.format("    %%-%ds : %%s%%n", maxLength);
        return VALUES.mapAll(regular -> String.format(format, regular.name(), regular.excerpt))
                     .collect(Collectors.joining())
                     .trim();
    }

    private static Supplier<BadConditionException> newBadArgsException(final Condition c) {
        return () -> BadConditionException.format(Regular.class, c.cmdLine(), c.cmdName(), excerpts());
    }

    private static Optional<Regular> findBy(final String subCmdName) {
        return VALUES.findAny(value -> value.name().equalsIgnoreCase(subCmdName));
    }

    private static Regular findBy(final Condition condition) throws BadConditionException {
        return condition.arg(1)
                        .flatMap(Regular::findBy)
                        .orElseThrow(newBadArgsException(condition));
    }

    public static Runnable job(final Condition condition) throws BadConditionException {
        return findBy(condition).toJob.apply(condition);
    }
}
