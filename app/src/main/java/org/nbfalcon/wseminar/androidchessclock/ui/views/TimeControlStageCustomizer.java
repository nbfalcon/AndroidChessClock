package org.nbfalcon.wseminar.androidchessclock.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.NumberPicker;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;

public class TimeControlStageCustomizer extends ConstraintLayout {
    public TimeControlStageCustomizer(@NonNull @NotNull Context context) {
        super(context);
    }

    public TimeControlStageCustomizer(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeControlStageCustomizer(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final TimePickerWithSeconds baseTime;
    private final NumberPicker increment;
    private final Spinner incrementType;

    {
        LayoutInflater.from(getContext()).inflate(R.layout.view_time_control_stage, this);
        baseTime = findViewById(R.id.base_time);
        increment = findViewById(R.id.increment);
        increment.setMinValue(0);
        increment.setMaxValue(180); // lichess.org has the same limit
        incrementType = findViewById(R.id.incrementType);
    }

    public long getIncrementMS() {
        return increment.getValue() * 1000L;
    }

    public long getBaseTimeMS() {
        return baseTime.getTimeSeconds() * 1000;
    }

    public TimeControlStageTemplate.Type getIncrementType() {
        return TimeControlStageTemplate.Type.values()[(int) incrementType.getSelectedItemId()];
    }

    public void bindFrom(SingleStageTimeControlTemplate timeControl) {
        // NOTE: we're losing precision; however, since there is no other way to input time
        baseTime.setTimeSeconds(timeControl.time / 1000);
        increment.setValue((int) (timeControl.increment / 1000));
        incrementType.setSelection(timeControl.type.ordinal());
    }

    public void setOnChangedListener(@Nullable OnChangedListener onChanged) {
        if (onChanged != null) {
            baseTime.setOnChangeListener((prevTimeS, newTimeS) -> {
                long incr = getIncrementMS();
                onChanged.onChanged(prevTimeS * 1000, incr, newTimeS * 1000, incr);
            });
            increment.setOnValueChangedListener(((picker, oldVal, newVal) -> {
                long base = getBaseTimeMS();
                onChanged.onChanged(base, oldVal * 1000L, base, newVal * 1000L);
            }));
        }
        else {
            baseTime.setOnChangeListener(null);
            increment.setOnValueChangedListener(null);
        }
    }

    public interface OnChangedListener {
        void onChanged(long prevTimeMS, long prevIncrMS, long newTimeMS, long newIncrMS);
    }
}
