package de.team33.messaging;

import java.util.function.Consumer;

public interface Register<MSG> {
    boolean add(Consumer<? super MSG> var1);

    int size();
}
