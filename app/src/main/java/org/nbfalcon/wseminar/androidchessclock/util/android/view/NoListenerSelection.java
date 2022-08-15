package org.nbfalcon.wseminar.androidchessclock.util.android.view;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import androidx.annotation.Nullable;

public class NoListenerSelection<Delegate extends AdapterView<DelegateAdapter>, DelegateAdapter extends Adapter> {
    private final Delegate delegate;
    private int noListenerItem = -1;

    public NoListenerSelection(Delegate delegate) {
        this.delegate = delegate;
    }

    public void setOnItemSelectedListener(@Nullable AdapterView.OnItemSelectedListener delegate) {
        this.delegate.setOnItemSelectedListener(delegate == null ? null : new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != noListenerItem) {
                    delegate.onItemSelected(parent, view, position, id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                delegate.onNothingSelected(parent);
            }
        });
    }

    public void setAdapter(DelegateAdapter adapter) {
        delegate.setAdapter(adapter);
    }

    public void setSelectionNoListener(int position) {
        noListenerItem = position;
        delegate.setSelection(position);
    }

    public void setSelectionWithListener(int position) {
        delegate.setSelection(position);
    }

    public int getCount() {
        return delegate.getCount();
    }

    public int getSelectedItemPosition() {
        return delegate.getSelectedItemPosition();
    }

    public Object getSelectedItem() {
        return delegate.getSelectedItem();
    }
}
