package de.team33.selection;

import de.team33.messaging.Register;

import java.util.Set;

public interface Selection<E> extends Set<E> {
    Register<Message<E>> getRegister();

    public interface Message<E> extends de.team33.messaging.Message<Selection<E>> {
    }
}
