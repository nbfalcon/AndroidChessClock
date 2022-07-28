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
        return increment.getValue();
    }

    public long getBaseTimeMS() {
        return baseTime.getTimeSeconds() * 1000;
    }

    public TimeControlStageTemplate.Type getIncrementType() {
        return TimeControlStageTemplate.Type.values()[(int) incrementType.getSelectedItemId()];
    }
}
