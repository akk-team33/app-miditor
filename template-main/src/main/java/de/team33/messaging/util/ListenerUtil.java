package de.team33.messaging.util;

import java.util.Iterator;
import java.util.function.Consumer;

public class ListenerUtil {
    public ListenerUtil() {
    }

    public static <MSG> void pass(Consumer<? super MSG> listener, Iterable<MSG> messages) {
        Iterator var3 = messages.iterator();

        while (var3.hasNext()) {
            MSG message = (MSG) var3.next();
            listener.accept(message);
        }

    }
}
