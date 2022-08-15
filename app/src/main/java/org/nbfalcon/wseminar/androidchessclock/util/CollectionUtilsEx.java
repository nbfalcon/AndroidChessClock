package org.nbfalcon.wseminar.androidchessclock.util;

import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

import java.util.Arrays;
import java.util.List;

public class CollectionUtilsEx {
    protected CollectionUtilsEx() {
    }

    public static <E> void move(List<E> list, int from, int to) {
        E moveMe = list.get(from);
        if (from < to) {
            for (int i = from + 1; i < to; i++) {
                list.set(i - 1, list.get(i));
            }
        } else if (to < from) {
            for (int i = from - 1; i > to; i--) {
                list.set(i + 1, list.get(i));
            }
        }
        list.set(to, moveMe);
    }

    /**
     * @apiNote Always use SimpleMutableList.move()! This is just to keep the implementations of move local to each other.
     */
    public static <E> void move(SimpleMutableList<E> list, int from, int to) {
        E moveMe = list.get(from);
        if (from < to) {
            for (int i = from + 1; i < to; i++) {
                list.set(i - 1, list.get(i));
            }
        } else if (to < from) {
            for (int i = from - 1; i > to; i--) {
                list.set(i + 1, list.get(i));
            }
        }
        list.set(to, moveMe);
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <R, S> @NotNull R[] downCastArray(@NotNull S[] source, @NotNull final R[] empty) {
        R[] result = Arrays.copyOf(empty, source.length);
        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }

    public static String join(String[] joinUs, String with) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < joinUs.length; i++) {
            if (i != 0) result.append(with);
            result.append(joinUs[i]);
        }
        return result.toString();
    }
}
