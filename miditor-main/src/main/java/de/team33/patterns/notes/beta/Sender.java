package de.team33.patterns.notes.beta;

import de.team33.patterns.building.elara.BuilderBase;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sender<S extends Sender<S>> extends BuilderBase<S> {

    private final Audience audience;

    /**
     * Initializes a new instance and checks the intended <em>sender</em> type for consistency.
     *
     * @param senderClass The {@link Class} representation of the intended effective <em>sender</em> type.
     * @throws IllegalArgumentException if the given <em>sender</em> class does not represent <em>this</em> instance.
     */
    protected Sender(final Class<S> senderClass, final Audience audience) {
        super(senderClass);
        this.audience = audience;
    }

    protected final Audience audience() {
        return audience;
    }

//    /**
//     * Returns a {@link Registry} where participants can register {@link Consumer}s as listeners
//     * to receive messages that are triggered based on specific events.
//     */
//    public final Registry registry() {
//        return audience;
//    }

    /**
     * Registers a <em>listener</em> with <em>this</em> sender to receive messages
     * transmitted over the given <em>channel</em>.
     *
     * @param <M> The message type.
     * @return <em>this</em> as an instance of the effective sender type {@code <S>}.
     * @see #remove(Collection)
     * @see #remove(Consumer[])
     */
    public final <M> S add(final Channel<? super S, M> channel, final Consumer<? super M> listener) {
        final M message = channel.apply(THIS());
        listener.accept(message);
        audience.add(channel, listener);
        return THIS();
    }

    /**
     * Unregisters several <em>listeners</em> from <em>this</em> sender to stop receiving messages
     * transmitted over any <em>channel</em>.
     * <p>
     * <em>CAUTION</em>: The <em>listeners</em> must each be identifiable by {@link Object#equals(Object)}
     * to be successfully unregistered!
     * It is strongly recommended to unregister the identical <em>listeners</em> that were registered!
     *
     * @return <em>this</em> as an instance of the effective sender type {@code <S>}.
     * @see #add(Channel, Consumer)
     * @see #remove(Consumer[])
     */
    public final S remove(final Collection<? extends Consumer<?>> listeners) {
        audience.remove(listeners);
        return THIS();
    }

    /**
     * Unregisters several <em>listeners</em> from <em>this</em> sender to stop receiving messages
     * transmitted over any <em>channel</em>.
     * <p>
     * <em>CAUTION</em>: The <em>listeners</em> must each be identifiable by {@link Object#equals(Object)}
     * to be successfully unregistered!
     * It is strongly recommended to unregister the identical <em>listeners</em> that were registered!
     *
     * @return <em>this</em> as an instance of the effective sender type {@code <S>}.
     * @see #add(Channel, Consumer)
     * @see #remove(Collection)
     */
    @SuppressWarnings("OverloadedVarargsMethod")
    public final S remove(final Consumer<?>... listeners) {
        return remove(Arrays.asList(listeners));
    }

    /**
     * Sends messages to all {@linkplain Consumer listeners} {@linkplain #add(Channel, Consumer) registered}
     * for any of the given <em>channels</em>.
     *
     * @return <em>this</em> as an instance of the effective sender type {@code <S>}.
     */
    protected final S fire(final Channel<?, ?>... channels) {
        return fire(Arrays.asList(channels));
    }

    /**
     * Sends messages to all {@linkplain Consumer listeners} {@linkplain #add(Channel, Consumer) registered}
     * for any of the given <em>channels</em>.
     *
     * @return <em>this</em> as an instance of the effective sender type {@code <S>}.
     */
    protected final S fire(final Iterable<? extends Channel<?, ?>> channels) {
        //noinspection rawtypes
        for (final Channel channel : channels) {
            //noinspection unchecked
            fire(channel);
        }
        return THIS();
    }

    private <M> void fire(final Channel<? super S, M> channel) {
        audience.send(channel, channel.apply(THIS()));
    }

    @FunctionalInterface
    public interface Channel<S, M> extends de.team33.patterns.notes.beta.Channel<M>, Function<S, M> {
    }

    private final class RegistryProxy implements Registry {

        @Override
        public <M> void add(final de.team33.patterns.notes.beta.Channel<M> channel,
                            final Consumer<? super M> listener) {
            if (channel instanceof final Sender.Channel<S,M> smChannel) {
                listener.accept(smChannel.apply(THIS()));
            }
            audience.add(channel, listener);
        }

        @Override
        public void remove(final de.team33.patterns.notes.beta.Channel<?> channel, final Consumer<?> listener) {
            audience.remove(channel, listener);
        }

        @Override
        public void remove(final Collection<? extends Consumer<?>> listeners) {
            audience.remove(listeners);
        }
    }
}
