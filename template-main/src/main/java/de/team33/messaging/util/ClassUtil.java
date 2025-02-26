package de.team33.messaging.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ClassUtil {
    public ClassUtil() {
    }

    private static Type[] actualTypeArguments(Class<?> genClass, Class<?> impClass) {
        Type superType;
        if (genClass.isInterface()) {
            Type[] var5;
            int var4 = (var5 = impClass.getGenericInterfaces()).length;

            int var3;
            Type[] ret;
            for(var3 = 0; var3 < var4; ++var3) {
                superType = var5[var3];
                if (superType instanceof ParameterizedType) {
                    ret = actualTypeArguments(genClass, (ParameterizedType)superType);
                    if (ret != null) {
                        return ret;
                    }
                }
            }

            Class[] var11;
            var4 = (var11 = impClass.getInterfaces()).length;

            for(var3 = 0; var3 < var4; ++var3) {
                Class<?> superInterface = var11[var3];
                if (genClass.isAssignableFrom(superInterface)) {
                    ret = actualTypeArguments(genClass, superInterface);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
        }

        if (!impClass.isInterface()) {
            superType = impClass.getGenericSuperclass();
            if (superType instanceof ParameterizedType) {
                Type[] ret = actualTypeArguments(genClass, (ParameterizedType)superType);
                if (ret != null) {
                    return ret;
                }
            }

            Class<?> superClass = impClass.getSuperclass();
            if (superClass != null && genClass.isAssignableFrom(superClass)) {
                Type[] ret = actualTypeArguments(genClass, superClass);
                if (ret != null) {
                    return ret;
                }
            }
        }

        return new Type[0];
    }

    private static Type[] actualTypeArguments(Class<?> genClass, ParameterizedType impType) {
        Type raw = impType.getRawType();
        if (raw instanceof Class) {
            Class<?> cRaw = (Class)raw;
            if (genClass.isAssignableFrom(cRaw)) {
                return impType.getActualTypeArguments();
            }
        }

        return null;
    }

    public static <B, I extends B> Class<?> getActualClassArgument(Class<B> genClass, Class<I> impClass) throws IllegalArgumentException {
        Type[] types = getActualTypeArguments(genClass, impClass);
        if (types.length == 1) {
            if (types[0] instanceof Class) {
                return (Class)types[0];
            } else {
                throw new AmbiguousArgumentException(impClass, types[0]);
            }
        } else if (types.length < 1) {
            throw new MissingArgumentsException(genClass, impClass);
        } else {
            throw new MultiParameterException(genClass, impClass, types);
        }
    }

    public static <B, I extends B> Type[] getActualTypeArguments(Class<B> genClass, Class<I> impClass) {
        return actualTypeArguments(genClass, impClass);
    }

    public static String getPathString(Class<?> type) {
        return type.getName().replaceAll("\\.", "/");
    }

    public static class AmbiguousArgumentException extends IllegalArgumentException {
        private AmbiguousArgumentException(Class<?> genClass, Type type) {
            super("Das von der Klasse " + genClass.getName() + " spezifizierte Typ-Argument (" + type + ") ist durch eine " + Class.class.getName() + "-Instanz nicht bzw. nicht eindeutig darstellbar");
        }
    }

    public static class MissingArgumentsException extends IllegalArgumentException {
        private MissingArgumentsException(Class<?> genClass, Class<?> impClass) {
            super("Die Implementationsklasse (" + impClass.getName() + ") definiert keine Typ-Argumente bezüglich der Basisklasse (" + genClass.getName() + ")");
        }
    }

    public static class MultiParameterException extends IllegalArgumentException {
        private MultiParameterException(Class<?> genClass, Class<?> impClass, Type[] types) {
            super("Die Implementationsklasse (" + impClass.getName() + ") erfüllt gegenüber der Basisklasse (" + genClass.getName() + ") nicht genau ein sondern " + types.length + " Klassen-Argumente: " + Arrays.toString(types));
        }
    }
}
