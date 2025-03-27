package de.team33.patterns.notes.beta;

import de.team33.patterns.building.gamma.SelfReferring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sender<S extends Sender<S>> extends SelfReferring<S> {

    private final Audience audience;
    private final Registry<?> registry;
    private final Set<Channel<? super S, ?>> initials;

    /**
     * Initializes a new instance and checks the intended <em>sender</em> type for consistency.
     *
     * @param senderClass The {@link Class} representation of the intended effective <em>sender</em> type.
     * @param audience    An {@link Audience} that is used to manage listeners.
     * @param initials    The {@link Channel}s on which an initial message is sent when a listener
     *                    {@linkplain Registry#add(de.team33.patterns.notes.beta.Channel, Consumer) registers}
     *                    with {@link #registry()}.
     * @throws IllegalArgumentException if the given <em>sender</em> class does not represent <em>this</em> instance.
     */
    protected Sender(final Class<S> senderClass, final Audience audience,
                     final Collection<? extends Channel<? super S, ?>> initials) {
        super(senderClass);
        this.audience = audience;
        this.registry = new RegistryProxy();
        this.initials = Set.copyOf(initials);
    }

    /**
     * Initializes a new instance and checks the intended <em>sender</em> type for consistency.
     *
     * @param senderClass The {@link Class} representation of the intended effective <em>sender</em> type.
     * @param executor    An {@link Executor} that is used to execute transmissions of messages to listeners.
     * @param initials    The {@link Channel}s on which an initial message is sent when a listener
     *                    {@linkplain Registry#add(de.team33.patterns.notes.beta.Channel, Consumer) registers}
     *                    with {@link #registry()}.
     * @throws IllegalArgumentException if the given <em>sender</em> class does not represent <em>this</em> instance.
     */
    protected Sender(final Class<S> senderClass, final Executor executor,
                     final Collection<? extends Channel<? super S, ?>> initials) {
        this(senderClass, new Audience(executor), initials);
    }

    protected final Audience audience() {
        return audience;
    }

    /**
     * Returns a {@link Registry} where participants can register {@link Consumer}s as listeners
     * to receive messages that are triggered based on specific events.
     * <p>
     * Listeners that register here may receive an initial message on being
     * {@link Registry#add(de.team33.patterns.notes.beta.Channel, Consumer) added}.
     */
    public final Registry<?> registry() {
        return registry;
    }

    /**
     * Sends messages to all {@linkplain Consumer listeners}
     * {@linkplain Registry#add(de.team33.patterns.notes.beta.Channel, Consumer) registered}
     * to the underlying {@linkplain #registry() registry}
     * for any of the given <em>channels</em>.
     *
     * @return <em>this</em> as an instance of the effective sender type {@code <S>}.
     */
    @SuppressWarnings("OverloadedVarargsMethod")
    protected final S fire(final Channel<?, ?>... channels) {
        return fire(Arrays.asList(channels));
    }

    /**
     * Sends messages to all {@linkplain Consumer listeners}
     * {@linkplain Registry#add(de.team33.patterns.notes.beta.Channel, Consumer) registered}
     * to the underlying {@linkplain #registry() registry}
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
        audience.fire(channel, channel.apply(THIS()));
    }

    @FunctionalInterface
    public interface Channel<S, M> extends de.team33.patterns.notes.beta.Channel<M>, Function<S, M> {
    }

    @SuppressWarnings("ReturnOfInnerClass")
    private final class RegistryProxy implements Registry<RegistryProxy> {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public <M> RegistryProxy add(final de.team33.patterns.notes.beta.Channel<M> channel,
                                     final Consumer<? super M> listener) {
            if (channel instanceof final Sender.Channel smChannel && initials.contains(smChannel)) {
                listener.accept((M) smChannel.apply(THIS()));
            }
            return audience.add(channel, listener)
                           .respond(this);
        }

        @Override
        public RegistryProxy remove(final Collection<? extends Consumer<?>> listeners) {
            return audience.remove(listeners)
                           .respond(this);
        }
    }
}
