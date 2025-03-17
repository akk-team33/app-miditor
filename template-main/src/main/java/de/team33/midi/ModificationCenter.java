package de.team33.midi;

import de.team33.patterns.notes.alpha.Audience;
import de.team33.patterns.notes.alpha.Registry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused")
class ModificationCenter {

    private final Audience audience;
    private final AtomicLong mainCounter = new AtomicLong(0L);
    private final Map<Integer, AtomicLong> subCounters = new ConcurrentHashMap<>(0);

    ModificationCenter(final Executor executor) {
        this.audience = new Audience(executor);
    }

    final Registry<?> registry() {
        return audience;
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
    }

    final void reset() {
        mainCounter.set(0L);
        subCounters.clear();
        audience.send(Channel.RESET, null);
    }

    final void keep(final Collection<Integer> ids) {
        subCounters.keySet().retainAll(ids);
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @FunctionalInterface
    interface Channel<M> extends de.team33.patterns.notes.alpha.Channel<M> {

        Channel<Void> MODIFIED = () -> Channel.class.getCanonicalName() + ":MODIFIED";
        Channel<Integer> SUB_MODIFIED = () -> Channel.class.getCanonicalName() + ":SUB_MODIFIED";
        Channel<Void> RESET = () -> Channel.class.getCanonicalName() + ":RESET";
    }
}
