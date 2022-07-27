package org.nbfalcon.wseminar.androidchessclock.util.views;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.SpinnerAdapter;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SectionedSpinner extends androidx.appcompat.widget.AppCompatSpinner {
    private final List<SpinnerAdapter> mySections = new ArrayList<>();

    public SectionedSpinner(Context context) {
        super(context);
    }

    public SectionedSpinner(Context context, int mode) {
        super(context, mode);
    }

    public SectionedSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SectionedSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setAdapter(new CompositeSpinnerAdapter());
    }

    private static class SectionDelimiterBG extends InsetDrawable {
        public SectionDelimiterBG(@Nullable Drawable drawable) {
            super(drawable, 0, 8, 0, 0);
        }
    }

    private class CompositeSpinnerAdapter implements SpinnerAdapter {
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return mapSection(position, (section, i1) -> section.getDropDownView(i1, convertView, parent));
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            for (SpinnerAdapter section : mySections) {
                section.registerDataSetObserver(dataSetObserver);
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            for (SpinnerAdapter section : mySections) {
                section.unregisterDataSetObserver(dataSetObserver);
            }
        }

        @Override
        public int getCount() {
            return sumSections(Adapter::getCount);
        }

        private <T> T mapSection(int i, BiFunction<SpinnerAdapter, Integer, T> mapper) {
            for (SpinnerAdapter section : mySections) {
                if (i < section.getCount()) {
                    return mapper.apply(section, i);
                } else {
                    i -= section.getCount();
                }
            }
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }

        private int sumSections(Function<SpinnerAdapter, Integer> mapper) {
            int count = 0;
            for (SpinnerAdapter section : mySections) {
                count += mapper.apply(section);
            }
            return count;
        }

        @Override
        public Object getItem(int i) {
            return mapSection(i, Adapter::getItem);
        }

        @Override
        public long getItemId(int i) {
            return mapSection(i, Adapter::getItemId);
        }

        @Override
        public boolean hasStableIds() {
            for (SpinnerAdapter section : mySections) {
                if (!section.hasStableIds()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            return mapSection(i, (section, i2) -> {
                View view = section.getView(i2, convertView, parent);
                Drawable bg = view.getBackground();

                if (i2 == 0) {
                    if (!(bg instanceof SectionDelimiterBG)) {
                        view.setBackground(new SectionDelimiterBG(bg));
                    }
                } else {
                    if (bg instanceof SectionDelimiterBG) {
                        view.setBackground(((SectionDelimiterBG) bg).getDrawable());
                    }
                }

                return view;
            });
        }

        @Override
        public int getItemViewType(int i) {
            return mapSection(i, Adapter::getItemViewType);
        }

        @Override
        public int getViewTypeCount() {
            return sumSections(Adapter::getViewTypeCount);
        }

        @Override
        public boolean isEmpty() {
            for (SpinnerAdapter section : mySections) {
                if (!section.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
}
