package de.team33.patterns.notes.alpha;

import java.util.Arrays;
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
 * Implementation of a registry with the additional option to send messages to the registered listeners.
 */
public class Audience implements Registry<Audience> {

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

    private static <M> Consumer<M> emitter(final Collection<? extends Consumer<? super M>> listeners) {
        return message -> {
            for (final Consumer<? super M> listener : listeners) {
                listener.accept(message);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <M> List<Consumer<? super M>> list(final Channel<M> channel) {
        return backing.get(channel);
    }

    @SuppressWarnings("unchecked")
    private <M> Stream<Consumer<? super M>> stream(final Channel<M> channel) {
        return Optional.ofNullable(list(channel)).map(List::stream).orElseGet(Stream::empty);
    }

    @Override
    public final <M> Audience add(final Channel<M> channel, final Consumer<? super M> listener) {
        synchronized (backing) {
            backing.put(channel, Stream.concat(stream(channel), Stream.of(listener)).toList());
        }
        return this;
    }

    private void remove(final Channel<?> channel, final Collection<? extends Consumer<?>> listeners) {
        backing.put(channel, stream(channel).filter(not(listeners::contains)).toList());
    }

    @Override
    public final Audience remove(final Channel<?> channel, final Consumer<?> listener) {
        synchronized (backing) {
            remove(channel, Set.of(listener));
        }
        return this;
    }

    @Override
    public final Audience remove(final Collection<? extends Consumer<?>> listeners) {
        synchronized (backing) {
            //noinspection unchecked,SuspiciousMethodCalls
            backing.entrySet().stream()
                   .filter(entry -> entry.getValue().stream()
                                         .anyMatch(listeners::contains))
                   .forEach(entry -> remove(entry.getKey(), listeners));
        }
        return this;
    }

    private <M> Optional<Consumer<M>> emitter(final Channel<? super M> channel) {
        synchronized (backing) {
            return Optional.ofNullable(list(channel))
                           .map(Audience::emitter);
        }
    }

    /**
     * Sends a given message to all listeners that have {@linkplain #add(Channel, Consumer) registered}
     * for the given {@link Channel}.
     *
     * @param <M> The message type.
     */
    public final <M> void send(final Channel<M> channel, final M message) {
        emitter(channel).ifPresent(emitter -> executor.execute(() -> emitter.accept(message)));
    }

    /**
     * TODO: revise!
     * <p>
     * Sends messages from the given {@link Mapping} to all listeners that have
     * {@linkplain #add(Channel, Consumer) registered} for one of the given {@link Channel channels}.
     */
    public final Consumer<Mapping> sendAll(final Channel<?>... channels) {
        return mapping -> sendAll(Arrays.asList(channels), mapping);
    }

    /**
     * Sends messages from the given {@link Mapping} to all listeners that have
     * {@linkplain #add(Channel, Consumer) registered} for one of the given {@link Channel channels}.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final void sendAll(final Iterable<? extends Channel<?>> channels, final Mapping mapping) {
        for (final Channel channel : channels) {
            send(channel, mapping.get(channel));
        }
    }
}
