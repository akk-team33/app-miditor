package de.team33.selection;

import de.team33.patterns.notes.eris.Audience;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;

public class SelectionImpl<E> extends AbstractSet<E> implements Selection<E> {

    private final HashSet<E> core;
    private final Audience audience = new Audience();

    public SelectionImpl() {
        this(new HashSet());
    }

    public SelectionImpl(final Collection<? extends E> c) {
        this(new HashSet(c));
    }

    private SelectionImpl(final HashSet<E> core) {
        this.core = core;
    }

    @Override
    public final void addListener(final Event event, final Consumer<? super Selection<?>> listener) {
        audience.add(event, listener);
        listener.accept(this);
    }

    public final boolean add(final E element) {
        final boolean result = core.add(element);
        if (result) {
            audience.send(Event.UPDATE, this);
        }
        return result;
    }

    public final boolean addAll(final Collection<? extends E> source) {
        final boolean result = core.addAll(source);
        if (result) {
            audience.send(Event.UPDATE, this);
        }
        return result;
    }

    public final void clear() {
        if (0 < core.size()) {
            super.clear();
            audience.send(Event.UPDATE, this);
        }
    }

    public final Iterator<E> iterator() {
        return new ITERATOR(core.iterator());
    }

    public final boolean remove(final Object element) {
        final boolean result = core.remove(element);
        if (result) {
            audience.send(Event.UPDATE, this);
        }
        return result;
    }

    public final boolean removeAll(final Collection<?> source) {
        final boolean result = core.removeAll(source);
        if (result) {
            audience.send(Event.UPDATE, this);
        }
        return result;
    }

    public final boolean retainAll(final Collection<?> source) {
        final boolean result = core.retainAll(source);
        if (result) {
            audience.send(Event.UPDATE, this);
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
            audience.send(Event.UPDATE, SelectionImpl.this);
        }
    }
}
