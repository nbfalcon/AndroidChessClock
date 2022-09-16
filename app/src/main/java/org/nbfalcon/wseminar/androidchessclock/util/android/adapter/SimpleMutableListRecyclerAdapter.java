package org.nbfalcon.wseminar.androidchessclock.util.android.adapter;

import androidx.recyclerview.widget.RecyclerView;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

public abstract class SimpleMutableListRecyclerAdapter<E, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements SimpleMutableList<E> {
    private final SimpleMutableList<E> backingList;

    public SimpleMutableListRecyclerAdapter(SimpleMutableList<E> backingList) {
        this.backingList = backingList;
    }

    @Override
    public long getItemId(int position) {
        return backingList.getRowId(position);
    }

    @Override
    public int getItemCount() {
        return size();
    }

    @Override
    public void add(E item) {
        backingList.add(item);
        notifyItemInserted(backingList.size() - 1);
    }

    @Override
    public void add(int index, E item) {
        backingList.add(index, item);
        notifyItemInserted(index);
    }

    @Override
    public void set(int index, E newValue) {
        backingList.set(index, newValue);
        notifyItemChanged(index);
    }

    @Override
    public E get(int index) {
        return backingList.get(index);
    }

    @Override
    public void remove(int index) {
        backingList.remove(index);
        notifyItemRemoved(index);
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
        int oldSize = size();
        backingList.clear();
        notifyItemRangeRemoved(0, oldSize);
    }

    @Override
    public void move(int from, int to) {
        backingList.move(from, to);
        notifyItemMoved(from, to);
    }

    @Override
    public E[] toArray(E[] a) {
        return backingList.toArray(a);
    }

    @Override
    public long getRowId(int pos) {
        return backingList.getRowId(pos);
    }
}
