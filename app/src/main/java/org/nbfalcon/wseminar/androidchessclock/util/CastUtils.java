package org.nbfalcon.wseminar.androidchessclock.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CastUtils {
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <R, S> @NotNull R[] downCastArray(@NotNull S[] source, @NotNull final R[] empty) {
        R[] result = Arrays.copyOf(empty, source.length);
        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }
}
