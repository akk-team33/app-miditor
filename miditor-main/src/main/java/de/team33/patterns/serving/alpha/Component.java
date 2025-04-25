package de.team33.patterns.serving.alpha;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class Component<C> implements Variable<C> {

    private final Executor executor;
    private final UnaryOperator<C> normalizer;
    private volatile List<Consumer<? super C>> listeners;
    private volatile C content;

    public Component(final Executor executor, final UnaryOperator<C> normalizer) {
        this.executor = executor;
        this.normalizer = normalizer;
    }

    @Override
    public final C get() {
        return content;
    }

    @Override
    public final void set(final C content) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public int subscribe(final Consumer<? super C> listener) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void unsubscribe(final int subscriberId) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
