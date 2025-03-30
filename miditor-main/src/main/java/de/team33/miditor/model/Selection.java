package de.team33.miditor.model;

import de.team33.patterns.notes.beta.Audience;
import de.team33.patterns.notes.beta.Channel;
import de.team33.patterns.notes.beta.Registry;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class Selection<E> extends AbstractSet<E> {

    private final HashSet<E> core;
    private final Audience audience = new Audience(Runnable::run);

    public Selection() {
        this(new HashSet());
    }

    public Selection(final Collection<? extends E> c) {
        this(new HashSet(c));
    }

    private Selection(final HashSet<E> core) {
        this.core = core;
    }

    public static <E> void set(final Selection<E> selection, final Collection<? extends E> elements) {
        selection.clear();
        selection.addAll(elements);
    }

    public final Registry<?> registry() {
        return audience;
    }

    public final boolean add(final E element) {
        final boolean result = core.add(element);
        if (result) {
            audience.fire(Event.UPDATE, this);
        }
        return result;
    }

    public final boolean addAll(final Collection<? extends E> source) {
        final boolean result = core.addAll(source);
        if (result) {
            audience.fire(Event.UPDATE, this);
        }
        return result;
    }

    public final void clear() {
        if (0 < core.size()) {
            super.clear();
            audience.fire(Event.UPDATE, this);
        }
    }

    public final Iterator<E> iterator() {
        return new ITERATOR(core.iterator());
    }

    public final boolean remove(final Object element) {
        final boolean result = core.remove(element);
        if (result) {
            audience.fire(Event.UPDATE, this);
        }
        return result;
    }

    public final boolean removeAll(final Collection<?> source) {
        final boolean result = core.removeAll(source);
        if (result) {
            audience.fire(Event.UPDATE, this);
        }
        return result;
    }

    public final boolean retainAll(final Collection<?> source) {
        final boolean result = core.retainAll(source);
        if (result) {
            audience.fire(Event.UPDATE, this);
        }
        return result;
    }

    public final int size() {
        return core.size();
    }

    private final class ITERATOR implements Iterator<E> {

        private final Iterator<E> core;

        ITERATOR(final Iterator<E> core) {
            this.core = core;
        }

        public final boolean hasNext() {
            return core.hasNext();
        }

        public final E next() {
            return core.next();
        }

        public final void remove() {
            core.remove();
            audience.fire(Event.UPDATE, Selection.this);
        }
    }

    public enum Event implements Channel<Selection<?>> {
        UPDATE
    }
}
