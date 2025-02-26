package de.team33.messaging;

public interface Register<MSG> {
    boolean add(Listener<? super MSG> var1);

    boolean remove(Object var1);

    int size();
}
