package org.nbfalcon.wseminar.androidchessclock.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;

public class TimerView extends AppCompatTextView {
    {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this, 12, 100, 1, TypedValue.COMPLEX_UNIT_SP);
        setGravity(Gravity.CENTER);
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
        // NOTE: no i18n; FIDE sadly does not say anything about required Arabic numerals though :(
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        String timeString = minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
        if (hours > 0) timeString = hours + ":" + timeString;

        return timeString;
    }

    public void setTime(long seconds) {
        String time = formatTime(seconds);
        setText(time);
    }
}
