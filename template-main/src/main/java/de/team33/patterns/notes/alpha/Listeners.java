package de.team33.patterns.notes.alpha;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Listeners {

    private final List<Consumer<?>> backing = new LinkedList<>();

    public <M> Consumer<M> add(final Consumer<M> consumer) {
        backing.add(consumer);
        return consumer;
    }

    public void removeFrom(final Registry<?> registry) {
        registry.remove(backing);
    }
}
