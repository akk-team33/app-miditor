package de.team33.midix;

import java.util.function.UnaryOperator;

class Mutable<T> {

    private final UnaryOperator<T> validator;
    private T value;

    Mutable(final UnaryOperator<T> validator) {
        this.validator = validator;
    }

    final T get() {
        return value;
    }

    final Mutable<T> set(final T value) {
        this.value = validator.apply(value);
        return this;
    }
}
