package org.nbfalcon.wseminar.androidchessclock.util.collections;

import java.util.Arrays;

public interface SimpleMutableList<E> {
    void add(E item);

    void add(int index, E item);

    void set(int index, E newValue);

    E get(int index);

    void remove(int index);


    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    void clear();

    default void move(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                E tmp = get(i);
                set(i, get(i + 1));
                set(i + 1, tmp);
            }
        } else if (to < from) {
            for (int i = from; i > to; i--) {
                E tmp = get(i);
                set(i, get(i - 1));
                set(i - 1, tmp);
            }
        }
    }

    // FIXME: probably optmize this
    static<E> E[] toArray(SimpleMutableList<E> src, E[] empty) {
        int length = src.size();
        E[] result = Arrays.copyOf(empty, length);
        for (int i = 0; i < length; i++) {
            result[i] = src.get(i);
        }
        return result;
    }
}
