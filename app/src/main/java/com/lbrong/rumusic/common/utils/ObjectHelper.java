package com.lbrong.rumusic.common.utils;

import java.util.Collection;

/**
 * Created by 1bRoNG on 2018/7/21.
 */
public final class ObjectHelper {
    /** Utility class. */
    private ObjectHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Verifies if the object is not null and returns it or throws a NullPointerException
     * with the given message.
     * @param <T> the value type
     * @param object the object to verify
     * @param message the message to use with the NullPointerException
     * @return the object itself
     * @throws NullPointerException if object is null
     */
    public static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    public static <T> boolean requireNonNull(T object) {
        return object != null;
    }

    public static <T,M> boolean requireNonNull(T obj1,M obj2) {
        return obj1 != null && obj2 != null;
    }

    public static <T extends Collection> boolean requireNonNull(T object) {
        return object != null && object.size() != 0;
    }

    /**
     * Compares two potentially null objects with each other using Object.equals.
     * @param o1 the first object
     * @param o2 the second object
     * @return the comparison result
     */
    public static boolean equals(Object o1, Object o2) { // NOPMD
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    /**
     * Returns the hashCode of a non-null object or zero for a null object.
     * @param o the object to get the hashCode for.
     * @return the hashCode
     */
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    /**
     * Compares two integer values similar to Integer.compare.
     * @param v1 the first value
     * @param v2 the second value
     * @return the comparison result
     */
    public static int compare(int v1, int v2) {
        return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
    }

    /**
     * Compares two long values similar to Long.compare.
     * @param v1 the first value
     * @param v2 the second value
     * @return the comparison result
     */
    public static int compare(long v1, long v2) {
        return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
    }
}
