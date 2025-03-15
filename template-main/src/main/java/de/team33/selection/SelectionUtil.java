package de.team33.selection;

import java.util.Arrays;
import java.util.Collection;

public class SelectionUtil {
    public SelectionUtil() {
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    @SafeVarargs
    public static <E> void set(final Selection<E> selection, final E... elements) {
        set(selection, Arrays.asList(elements));
    }

    public static <E> void set(final Selection<E> selection, final Collection<? extends E> elements) {
        selection.clear();
        selection.addAll(elements);
    }
}
