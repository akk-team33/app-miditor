package de.team33.patterns.features.beta;

import de.team33.patterns.building.elara.BuilderBase;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class FeaturesBase<F extends BuilderBase<F>> extends BuilderBase<F> {

    @SuppressWarnings("rawtypes")
    private final Map<Key, Object> backing;

    /**
     * Initializes a new instance and checks the intended <em>Features</em> type for consistency.
     *
     * @param featuresClass The {@link Class} representation of the intended effective <em>Features</em> type.
     * @param newBacking    A {@link Supplier} to supply a new {@link Map} as backing.
     *                      In most cases it is recommended to supply a new {@link ConcurrentHashMap}.
     * @throws IllegalArgumentException if the given builder class does not represent <em>this</em> instance.
     * @see #FeaturesBase(Class)
     */
    protected FeaturesBase(final Class<F> featuresClass, final Supplier<Map<Key, Object>> newBacking) {
        super(featuresClass);
        backing = newBacking.get();
    }

    /**
     * Initializes a new instance backed by a new {@link ConcurrentHashMap} and checks
     * the intended <em>Features</em> type for consistency.
     *
     * @param featuresClass The {@link Class} representation of the intended effective <em>Features</em> type.
     * @throws IllegalArgumentException if the given builder class does not represent <em>this</em> instance.
     * @see #FeaturesBase(Class, Supplier)
     */
    protected FeaturesBase(final Class<F> featuresClass) {
        this(featuresClass, ConcurrentHashMap::new);
    }

    private <R> R initial(final Key<? super F, R> key) {
        return key.initial(THIS());
    }

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
    public final <R> R get(final Key<? super F, R> key) {
        return (R) backing.computeIfAbsent(key, this::initial);
    }

    @SuppressWarnings("unchecked")
    public final <R> Optional<R> peek(final Key<? super F, R> key) {
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
    public interface Key<F, R> {

        R initial(F features);
    }
}
