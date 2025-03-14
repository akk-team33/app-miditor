package de.team33.patterns.mutable.alpha;

import java.util.function.UnaryOperator;

public class Mutable<V> {

    private final UnaryOperator<V> normalizer;
    private V value;

    public Mutable(final UnaryOperator<V> normalizer, final V value) {
        this.normalizer = normalizer;
        this.value = normalizer.apply(value);
    }

    public final V get() {
        return value;
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    public final Mutable<V> set(final V value) {
        this.value = normalizer.apply(value);
        return this;
    }
}
