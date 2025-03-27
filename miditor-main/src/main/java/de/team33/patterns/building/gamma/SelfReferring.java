package de.team33.patterns.building.gamma;

/**
 * Base class of a generic class hierarchy that has a type parameter intended to represent the effective type of
 * concrete implementation.
 * <p>
 * The main purpose is the realization of a generic builder pattern, whereby there will be methods that should
 * result in {@link #THIS()} - i.e. the builder instance itself - in order to chain further method calls.
 * <p>
 * For this purpose, the constructor ensures that <em>this</em> actually corresponds to the designated type.
 *
 * @param <R> The builder type: the intended effective type of the concrete builder implementation.
 */
public class SelfReferring<R extends SelfReferring<R>> {

    private static final String ILLEGAL_REF_CLASS =
            "<refClass> is expected to represent the effective class of <this> (%s) - but was %s";

    /**
     * Initializes a new instance and checks the intended self referring type for consistency.
     *
     * @param refClass The {@link Class} representation of the intended self referring type.
     * @throws IllegalArgumentException if <em>refClass</em> does not represent <em>this</em> instance.
     */
    protected SelfReferring(final Class<R> refClass) {
        if (!refClass.isAssignableFrom(getClass())) {
            throw new IllegalArgumentException(String.format(ILLEGAL_REF_CLASS, getClass(), refClass));
        }
    }

    /**
     * Returns <em>this</em> as an instance of the self referring type {@code <R>}.
     */
    @SuppressWarnings("unchecked")
    protected final R THIS() {
        return (R) this;
    }
}
