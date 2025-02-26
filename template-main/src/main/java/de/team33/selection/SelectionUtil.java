package de.team33.selection;

import java.util.Arrays;

public class SelectionUtil {
    public SelectionUtil() {
    }

    public static <E> void set(Selection<E> selection, E... elements) {
        selection.clear();
        selection.addAll(Arrays.asList(elements));
    }
}
