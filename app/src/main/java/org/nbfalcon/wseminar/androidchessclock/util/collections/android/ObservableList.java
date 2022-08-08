package org.nbfalcon.wseminar.androidchessclock.util.collections.android;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.util.Consumer;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

import java.util.ArrayList;

public class ObservableList<E> implements SimpleMutableList<E> {
    private final @NotNull SimpleMutableList<E> backingList;

    private final ArrayList<RecyclerView.AdapterDataObserver> observers = new ArrayList<>();

    public ObservableList(@NotNull SimpleMutableList<E> backingList) {
        this.backingList = backingList;
    }

    public void registerAdapterDataObserver(@NotNull RecyclerView.AdapterDataObserver observer) {
        synchronized (observers) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    public void unregisterAdapterDataObserver(@NotNull RecyclerView.AdapterDataObserver observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    private void fire(Consumer<RecyclerView.AdapterDataObserver> invoke) {
        // Same as AdapterDataObservable, we iterate in reverse to allow observers to unregister themselves without
        //  invalidating indexes;
        for (int i = observers.size() - 1; i >= 0; i--) {
            RecyclerView.AdapterDataObserver observer = observers.get(i);
            invoke.accept(observer);
        }
    }

    @Override
    public void add(E item) {
        backingList.add(item);
        fire(observer -> observer.onItemRangeInserted(backingList.size() - 1, 1));
    }

    @Override
    public void add(int index, E item) {
        backingList.add(index, item);
        fire(observer -> observer.onItemRangeInserted(index, 1));
    }

    @Override
    public void set(int index, E newValue) {
        backingList.set(index, newValue);
        fire(observer -> observer.onItemRangeChanged(index, 1));
    }

    @Override
    public E get(int index) {
        return backingList.get(index);
    }

    @Override
    public void remove(int index) {
        backingList.remove(index);
        fire(observer -> observer.onItemRangeRemoved(index, 1));
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean isEmpty() {
        return backingList.isEmpty();
    }

    @Override
    public void clear() {
        final int sizeBefore = size();
        backingList.clear();
        fire(observer -> observer.onItemRangeRemoved(0, sizeBefore));
    }

    public static RecyclerView.AdapterDataObserver observerFromAdapter(RecyclerView.Adapter<?> adapter) {
        return new RecyclerView.AdapterDataObserver() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged() {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                adapter.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable @org.jetbrains.annotations.Nullable Object payload) {
                adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                adapter.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                adapter.notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                adapter.notifyItemRangeRemoved(fromPosition, itemCount);
                adapter.notifyItemRangeInserted(toPosition < fromPosition ? toPosition : toPosition - itemCount, itemCount);
            }
        };
    }
}