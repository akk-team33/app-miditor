package de.team33.patterns.serving.alpha;

import de.team33.patterns.collection.ceres.Collecting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;

/**
 * Implementation of a {@link Subscribable} with the additional option to send messages to subscribers.
 *
 * @param <M> The message type.
 */
public class Audience<M> implements Subscribable<M> {

    private final Executor executor;
    private volatile List<Consumer<? super M>> backing = List.of();

    public Audience(final Executor executor) {
        this.executor = executor;
    }

    private static <M> Runnable emitter(final Collection<? extends Consumer<? super M>> listeners, final M message) {
        return () -> {
            for (final Consumer<? super M> listener : listeners) {
                listener.accept(message);
            }
        };
    }

    @SuppressWarnings("SynchronizedMethod")
    @Override
    public final synchronized int subscribe(final Consumer<? super M> listener) {
        backing = Collecting.charger(new ArrayList<Consumer<? super M>>(backing.size() + 1))
                            .addAll(backing)
                            .add(listener)
                            .charged();
        return System.identityHashCode(listener);
    }

    @SuppressWarnings("SynchronizedMethod")
    @Override
    public final synchronized void unsubscribe(final int subscriberId) {
        backing = Collecting.charger(new ArrayList<>(backing))
                            .removeIf(listener -> subscriberId == System.identityHashCode(listener))
                            .charged();
    }

    @SuppressWarnings("SynchronizedMethod")
    private synchronized Optional<Runnable> emitter(final M message) {
        return Optional.of(backing)
                       .filter(not(List::isEmpty))
                       .map(listeners -> emitter(listeners, message));
    }

    /**
     * Sends a given message to all listeners that have {@linkplain #subscribe(Consumer) subscribed}.
     */
    public final void fire(final M message) {
        emitter(message).ifPresent(executor::execute);
    }
}
