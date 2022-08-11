package org.nbfalcon.wseminar.androidchessclock.util.android;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ThemedSpinnerAdapter;
import org.nbfalcon.wseminar.androidchessclock.util.collections.android.ObservableList;

public class SimpleMutableListAdapter<E> extends BaseAdapter implements ThemedSpinnerAdapter {
    private final ObservableList<E> backingList;
    private @LayoutRes
    final int itemLayout;
    private @LayoutRes
    final int dropDownViewLayout;
    private final ThemedSpinnerAdapter.Helper myThemedSpinnerAdapterHelper;
    private @Nullable E bonusItem;

    public SimpleMutableListAdapter(ObservableList<E> backingList, Context context, @LayoutRes int itemLayout, int dropDownViewLayout) {
        this.backingList = backingList;

        this.itemLayout = itemLayout;
        this.dropDownViewLayout = dropDownViewLayout;

        this.myThemedSpinnerAdapterHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    public void setBonusItem(@Nullable E item) {
        this.bonusItem = item;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        backingList.registerAdapterDataObserver(ObservableList.asAdapterDataObserver(observer));
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // FIXME: not implemented
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View newView = createView(convertView, parent, dropDownViewLayout);
        bindTextView(position, newView);
        return newView;
    }

    private View createView(View convertView, ViewGroup parent, @LayoutRes int layoutRes) {
        View newView;
        if (convertView != null) {
            newView = convertView;
        } else {
            newView = myThemedSpinnerAdapterHelper.getDropDownViewInflater().inflate(layoutRes, parent, false);
        }
        return newView;
    }

    private void bindTextView(int position, View newView) {
        TextView text = newView.findViewById(android.R.id.text1);
        text.setText(getItem(position).toString());
    }

    @Override
    public int getCount() {
        return backingList.size() + (bonusItem != null ? 1 : 0);
    }

    @Override
    public E getItem(int position) {
        if (position < backingList.size()) {
            return backingList.get(position);
        } else {
            return bonusItem;
        }
    }

    @Override
    public long getItemId(int position) {
        return position; // FIXME: monotonic?
    }

    @Override
    public boolean hasStableIds() {
        return false; // FIXME: see above: monotonic -> return true
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = createView(convertView, parent, itemLayout);
        bindTextView(position, newView);
        return newView;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return myThemedSpinnerAdapterHelper.getDropDownViewTheme();
    }

    @Override
    public void setDropDownViewTheme(@Nullable @org.jetbrains.annotations.Nullable Resources.Theme theme) {
        myThemedSpinnerAdapterHelper.setDropDownViewTheme(theme);
    }
}
