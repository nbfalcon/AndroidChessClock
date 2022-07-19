package org.nbfalcon.wseminar.androidchessclock.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;

public class TimerView extends AppCompatTextView {
    {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this, 12, 100, 1, TypedValue.COMPLEX_UNIT_SP);
    }

    public TimerView(Context context) {
        super(context);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static String formatTime(long seconds) {
        final long s = seconds % 60;
        seconds /= 60;
        final long m = seconds % 60;
        seconds /= 60;
        final long h = seconds % 24;
        seconds /= 24;
        final long d = seconds;

        String timeString = m + ":" + s;
        if (h > 0) timeString = h + ":" + timeString;
        if (d > 0) timeString = d + "d " + timeString;

        return timeString;
    }

    public void setTime(long seconds) {
        String time = formatTime(seconds);
        setText(time);
    }
}
