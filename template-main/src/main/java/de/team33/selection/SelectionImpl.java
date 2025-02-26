package de.team33.selection;

import de.team33.messaging.Register;
import de.team33.messaging.sync.Distributor;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class SelectionImpl<E> extends AbstractSet<E> implements Selection<E> {
    private final HashSet<E> core;
    private final Distributor<Selection.Message<E>> distributor;
    private final Selection.Message<E> message;

    public SelectionImpl() {
        this(new HashSet());
    }

    public SelectionImpl(Collection<? extends E> c) {
        this(new HashSet(c));
    }

    private SelectionImpl(HashSet<E> core) {
        this.distributor = new Distributor();
        this.message = new MESSAGE();
        this.core = core;
        this.distributor.addInitials(Arrays.asList(this.message));
    }

    public final boolean add(E element) {
        boolean result;
        if (result = this.core.add(element)) {
            this.distributor.accept(this.message);
        }

        return result;
    }

    public final boolean addAll(Collection<? extends E> source) {
        boolean result;
        if (result = this.core.addAll(source)) {
            this.distributor.accept(this.message);
        }

        return result;
    }

    public final void clear() {
        if (this.core.size() != 0) {
            super.clear();
            this.distributor.accept(this.message);
        }

    }

    public final Register<Selection.Message<E>> getRegister() {
        return this.distributor;
    }

    public final Iterator<E> iterator() {
        return new ITERATOR(this.core.iterator());
    }

    public final boolean remove(Object element) {
        boolean result;
        if (result = this.core.remove(element)) {
            this.distributor.accept(this.message);
        }

        return result;
    }

    public final boolean removeAll(Collection<?> source) {
        boolean result;
        if (result = this.core.removeAll(source)) {
            this.distributor.accept(this.message);
        }

        return result;
    }

    public final boolean retainAll(Collection<?> source) {
        boolean result;
        if (result = this.core.retainAll(source)) {
            this.distributor.accept(this.message);
        }

        return result;
    }

    public int size() {
        return this.core.size();
    }

    private final class ITERATOR implements Iterator<E> {
        private final Iterator<E> core;

        public ITERATOR(Iterator<E> core) {
            this.core = core;
        }

        public final boolean hasNext() {
            return this.core.hasNext();
        }

        public final E next() {
            return this.core.next();
        }

        public final void remove() {
            this.core.remove();
            SelectionImpl.this.distributor.accept(SelectionImpl.this.message);
        }
    }

    private class MESSAGE implements Selection.Message<E> {
        private MESSAGE() {
        }

        public final Selection<E> getSender() {
            return SelectionImpl.this;
        }
    }
}
