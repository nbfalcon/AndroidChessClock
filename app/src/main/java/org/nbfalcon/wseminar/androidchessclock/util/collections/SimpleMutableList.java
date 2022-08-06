package org.nbfalcon.wseminar.androidchessclock.util.collections;

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
}
