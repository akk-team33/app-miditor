package de.team33.patterns.features.alpha;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A tool that manages the <em>features</em> in the context of a specific <em>host</em>.
 *
 * @param <H> The type of the host.
 */
public abstract class Features<H> {

    @SuppressWarnings("rawtypes")
    private final Map<Key, Object> backing;

    @SuppressWarnings("rawtypes")
    protected Features(final Supplier<Map<Key, Object>> newBacking) {
        backing = newBacking.get();
    }

    private <R> R initialOf(final Key<? super H, R> key) {
        return key.init(host());
    }

    protected abstract H host();

    /**
     * Returns the <em>feature</em> that is associated with the given <em>key</em>.
     * <p>
     * When the <em>feature</em> in question is requested for the first time, it is created.
     * Once created, the same <em>feature</em> is returned until it is {@linkplain #reset(Key) reset}.
     *
     * @param key A unique key for a specific feature and also a method that can generate that feature
     *            on demand in the context of a specific host.
     *            <p>
     *            In general, the key is expected to have an identity semantic and is defined as a permanent
     *            constant. It is not a good idea to generate the key inline as its identity is then ambiguous.
     * @param <R> The type of the <em>feature</em>.
     */
    @SuppressWarnings("unchecked")
    public final <R> R get(final Key<? super H, R> key) {
        return (R) backing.computeIfAbsent(key, this::initialOf);
    }

    /**
     * TODO!
     */
    @SuppressWarnings("unchecked")
    public final <R> Optional<R> peek(final Key<? super H, R> key) {
        return Optional.ofNullable((R) backing.get(key));
    }

    /**
     * Resets the <em>feature</em> that is associated with the given <em>key</em>.
     * <p>
     * When the <em>feature</em> in question is requested for the next time, it is newly created.
     */
    public final void reset(final Key<?, ?> key) {
        backing.remove(key);
    }

    /**
     * Resets all <em>features</em> that are hosted by <em>this</em>.
     * <p>
     * When any <em>feature</em> is requested for the next time, it is newly created.
     */
    public final void reset() {
        backing.clear();
    }

    @FunctionalInterface
    public interface Key<H, R> {

        R init(H host);
    }
}
