package org.nbfalcon.wseminar.androidchessclock.util.collections;

import org.nbfalcon.wseminar.androidchessclock.util.CollectionUtilsEx;

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
        CollectionUtilsEx.move(this, from, to);
    }

    default E[] toArray(E[] a) {
        int length = size();
        E[] result = Arrays.copyOf(a, length);
        for (int i = 0; i < length; i++) {
            result[i] = get(i);
        }
        return result;
    }
}
