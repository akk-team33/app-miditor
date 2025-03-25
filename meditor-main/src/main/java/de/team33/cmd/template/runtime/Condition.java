package de.team33.cmd.template.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Abstracts the runtime information required to execute a job.
 */
public interface Condition {

    /**
     * Returns the runtime information required to execute a job.
     *
     * @param out  The {@link Output} to be used.
     * @param args The command line arguments.
     * @throws BadConditionException if there are no command line arguments.
     */
    static Condition of(final Output out, final String[] args) throws BadConditionException {
        return new ConditionImpl(out, args);
    }

    /**
     * The associated {@link Output}.
     */
    Output out();

    /**
     * The given command line arguments.
     */
    List<String> args();

    /**
     * Determines whether a certain number of command line arguments are given.
     */
    default boolean isSize(final int size) {
        return isSize(size, size);
    }

    /**
     * Determines whether the number of given command line arguments is in a specified range.
     */
    default boolean isSize(final int minSize, final int maxSize) {
        final int size = args().size();
        return (minSize <= size) && (size <= maxSize);
    }

    /**
     * Optionalizes this runtime information so that it is only {@linkplain Optional#isPresent() present}
     * if a certain number of {@linkplain #args() command line arguments} are given.
     */
    default Optional<Condition> limited(final int size) {
        return limited(size, size);
    }

    /**
     * Optionalizes this runtime information so that it is only {@linkplain Optional#isPresent() present}
     * if the number of given {@linkplain #args() command line arguments} is in a certain range.
     */
    default Optional<Condition> limited(final int minSize, final int maxSize) {
        return Optional.of(this)
                       .filter(cnd -> cnd.isSize(minSize, maxSize));
    }

    /**
     * Returns the optional argument with the given <em>index</em>, if {@linkplain Optional#isPresent() present}.
     */
    default Optional<String> arg(final int index) {
        return args().stream().skip(index).findFirst();
    }

    /**
     * Returns the reconstructed command line.
     */
    default String cmdLine() {
        return String.join(" ", args());
    }

    /**
     * Returns the name of the command.
     */
    default String cmdName() {
        final Path path = arg(0).map(Path::of)
                                .orElseThrow(() -> new IllegalStateException("cmdName not available"));
        return Optional.of(path)
                       .filter(Path::isAbsolute)
                       .map(Path::getFileName)
                       .orElse(path)
                       .toString();
    }
}
