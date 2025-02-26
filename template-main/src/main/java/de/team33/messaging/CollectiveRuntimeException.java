package de.team33.messaging;

import net.team33.aggregate.iterator.ImmutableIterator;
import net.team33.aggregate.iterator.ImmutableIteratorProxy;
import net.team33.aggregate.list.ImmutableList;
import net.team33.aggregate.list.ImmutableListProxyBase;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class CollectiveRuntimeException extends RuntimeException implements Iterable<Throwable> {
    private final List<Throwable> causes;
    private final ImmutableList<Throwable> causesProxy;

    private static Throwable getFirst(Iterator<? extends Throwable> iterator) {
        return iterator.hasNext() ? (Throwable)iterator.next() : null;
    }

    private static String getText(Iterator<? extends Throwable> iterator) {
        Throwable cause = getFirst(iterator);
        return cause == null ? null : cause.getMessage();
    }

    public CollectiveRuntimeException(Collection<? extends Throwable> causes) {
        this(getText(causes.iterator()), causes);
    }

    public CollectiveRuntimeException(String message, Collection<? extends Throwable> causes) {
        super(message, getFirst(causes.iterator()));
        this.causesProxy = new PROXY();
        this.causes = new Vector(causes);
    }

    public final ImmutableList<Throwable> getCauses() {
        return this.causesProxy;
    }

    public final ImmutableIterator<Throwable> iterator() {
        return new ImmutableIteratorProxy(this.causes.iterator());
    }

    public final int size() {
        return this.causes.size();
    }

    public final String toString() {
        StringBuilder buffer = new StringBuilder(super.toString());
        buffer.append(" {");
        Iterator var3 = this.causes.iterator();

        while(var3.hasNext()) {
            Throwable cause = (Throwable)var3.next();
            buffer.append("\n\t");
            buffer.append(cause.toString());
        }

        buffer.append("\n}");
        return buffer.toString();
    }

    private class PROXY extends ImmutableListProxyBase<Throwable> {
        private PROXY() {
        }

        protected List<? extends Throwable> getCore() {
            return CollectiveRuntimeException.this.causes;
        }
    }
}
