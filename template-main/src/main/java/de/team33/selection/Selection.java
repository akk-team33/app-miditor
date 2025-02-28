package de.team33.selection;

import de.team33.patterns.notes.eris.Channel;

import java.util.Set;
import java.util.function.Consumer;

public interface Selection<E> extends Set<E> {

    void addListener(Event event, Consumer<? super Selection<?>> listener);

    enum Event implements Channel<Selection<?>> {
        UPDATE
    }
}
