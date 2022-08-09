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
}
