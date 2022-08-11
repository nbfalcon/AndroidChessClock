package org.nbfalcon.wseminar.androidchessclock.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CollectionUtilsEx {
    protected CollectionUtilsEx() {
    }

    public static <E> void move(List<E> list, int from, int to) {
        // FIXME: optimize dis
        if (from < to) {
            for (int i = from; i < to; i++) {
                E tmp = list.get(i);
                list.set(i, list.get(i + 1));
                list.set(i + 1, tmp);
            }
        } else if (to < from) {
            for (int i = from; i > to; i--) {
                E tmp = list.get(i);
                list.set(i, list.get(i - 1));
                list.set(i - 1, tmp);
            }
        }
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
