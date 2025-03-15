package de.team33.midi;

import de.team33.patterns.notes.alpha.Audience;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("unused")
class ModificationCounter {

    private final Audience audience;
    private final AtomicLong mainCounter = new AtomicLong(0L);
    private final Map<Integer, AtomicLong> subCounters = new ConcurrentHashMap<>(0);

    ModificationCounter() {
        this.audience = new Audience();
    }

    @SuppressWarnings("TypeMayBeWeakened")
    final <M> void add(final Channel<M> channel, final Consumer<? super M> listener) {
        audience.add(channel, listener);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    final <M> void remove(final Channel<M> channel, final Consumer<? super M> listener) {
        audience.remove(channel, listener);
    }

    private AtomicLong subCounter(final int id) {
        return subCounters.computeIfAbsent(id, any -> new AtomicLong(0L));
    }

    final long get() {
        return mainCounter.get();
    }

    final long get(final int id) {
        return subCounter(id).get();
    }

    final void increment() {
        mainCounter.incrementAndGet();
        audience.send(Channel.MODIFIED, null);
    }

    final void increment(final int id) {
        subCounter(id).incrementAndGet();
        audience.send(Channel.SUB_MODIFIED, id);
        increment();
    }

    final void reset() {
        mainCounter.set(0L);
        subCounters.clear();
        audience.send(Channel.RESET, null);
    }

    final void keep(final Collection<Integer> ids) {
        final Set<Integer> drop = subCounters.keySet().stream()
                                             .filter(not(ids::contains))
                                             .collect(toSet());
        subCounters.keySet().removeAll(drop);
        audience.send(Channel.REMOVED, drop);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    interface Channel<M> extends de.team33.patterns.notes.alpha.Channel<M> {

        Channel<Void> MODIFIED = () -> Channel.class.getCanonicalName() + ":MODIFIED";
        Channel<Integer> SUB_MODIFIED = () -> Channel.class.getCanonicalName() + ":SUB_MODIFIED";
        Channel<Set<Integer>> REMOVED = () -> Channel.class.getCanonicalName() + ":REMOVED";
        Channel<Void> RESET = () -> Channel.class.getCanonicalName() + ":RESET";
    }
}
