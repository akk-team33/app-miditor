package de.team33.midi.util;

public final class ClassUtil {

    private ClassUtil() {
    }

    public static String getPathString(final Class<?> type) {
        return type.getName().replaceAll("\\.", "/");
    }
}
