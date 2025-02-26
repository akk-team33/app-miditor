package de.team33.messaging;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CollectiveRuntimeException extends RuntimeException implements Iterable<Throwable> {
    private final List<Throwable> causes;

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
        this.causes = new LinkedList<>(causes);
    }

    public final List<Throwable> getCauses() {
        return Collections.unmodifiableList(causes);
    }

    public final Iterator<Throwable> iterator() {
        return getCauses().iterator();
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
}
