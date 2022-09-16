package org.nbfalcon.wseminar.androidchessclock.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;

public class TimePickerWithSeconds extends LinearLayout {

    public TimePickerWithSeconds(Context context) {
        super(context);
    }

    public TimePickerWithSeconds(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePickerWithSeconds(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final @NotNull NumberPicker pickerHours;

    private final @NotNull NumberPicker pickerMinutes;

    private final @NotNull NumberPicker pickerSeconds;

    {
        LayoutInflater.from(getContext()).inflate(R.layout.view_time_picker_with_seconds, this);

        pickerHours = findViewById(R.id.pickerHours);
        pickerHours.setMinValue(0);
        pickerHours.setMaxValue(23);
        pickerMinutes = findViewById(R.id.pickerMinutes);
        pickerMinutes.setMinValue(0);
        pickerMinutes.setMaxValue(59);
        pickerSeconds = findViewById(R.id.pickerSeconds);
        pickerSeconds.setMinValue(0);
        pickerSeconds.setMaxValue(59);
    }

    public long getTimeSeconds() {
        return pickerHours.getValue() * 60 * 60L + pickerMinutes.getValue() * 60L + pickerSeconds.getValue();
    }

    public void setTimeSeconds(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;

        pickerHours.setValue((int) hours);
        pickerMinutes.setValue((int) minutes);
        pickerSeconds.setValue((int) seconds);
    }

    public void addTime(long deltaTS) {
        setTimeSeconds(Math.max(0, getTimeSeconds() + deltaTS));
    }
}
