package de.team33.patterns.notes.beta;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * Implementation of a {@link Registry} with the additional option to send messages to the registered listeners.
 */
public class Audience implements Registry {

    @SuppressWarnings("rawtypes")
    private final Map<Channel, List> backing = new HashMap<>(0);
    private final Executor executor;

    /**
     * Initializes a new instance that synchronously {@linkplain #send(Channel, Object) sends} messages to the
     * {@linkplain #add(Channel, Consumer) registered} listeners.
     */
    public Audience() {
        this(Runnable::run);
    }

    /**
     * Initializes a new instance that {@linkplain #send(Channel, Object) sends} messages to the
     * {@linkplain #add(Channel, Consumer) registered} listeners using a given {@link Executor}.
     */
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

    @SuppressWarnings("unchecked")
    private <M> List<Consumer<? super M>> list(final Channel<M> channel) {
        return backing.get(channel);
    }

    private <M> Stream<Consumer<? super M>> stream(final Channel<M> channel) {
        return Optional.ofNullable(list(channel)).map(List::stream).orElseGet(Stream::empty);
    }

    @Override
    public final <M> void add(final Channel<M> channel, final Consumer<? super M> listener) {
        synchronized (backing) {
            backing.put(channel, Stream.concat(stream(channel), Stream.of(listener)).toList());
        }
    }

    private void remove(final Channel<?> channel, final Collection<? extends Consumer<?>> listeners) {
        backing.put(channel, stream(channel).filter(not(listeners::contains)).toList());
    }

    @Override
    public final void remove(final Channel<?> channel, final Consumer<?> listener) {
        synchronized (backing) {
            remove(channel, Set.of(listener));
        }
    }

    @Override
    public final void remove(final Collection<? extends Consumer<?>> listeners) {
        synchronized (backing) {
            //noinspection unchecked,SuspiciousMethodCalls
            backing.entrySet().stream()
                   .filter(entry -> entry.getValue().stream()
                                         .anyMatch(listeners::contains))
                   .forEach(entry -> remove(entry.getKey(), listeners));
        }
    }

    private <M> Optional<Runnable> emitter(final Channel<? super M> channel, final M message) {
        synchronized (backing) {
            return Optional.ofNullable(list(channel))
                           .map(listeners -> emitter(listeners, message));
        }
    }

    /**
     * Sends a given message to all listeners that have {@linkplain #add(Channel, Consumer) registered}
     * for the given {@link Channel}.
     *
     * @param <M> The message type.
     */
    public final <M> void send(final Channel<M> channel, final M message) {
        emitter(channel, message).ifPresent(executor::execute);
    }
}
