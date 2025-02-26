package de.team33.messaging.util;

import de.team33.messaging.Listener;

import java.util.Iterator;

public class ListenerUtil {
    public ListenerUtil() {
    }

    public static <MSG> void pass(Listener<? super MSG> listener, Iterable<MSG> messages) {
        Iterator var3 = messages.iterator();

        while(var3.hasNext()) {
            MSG message = (MSG)var3.next();
            listener.pass(message);
        }

    }
}
