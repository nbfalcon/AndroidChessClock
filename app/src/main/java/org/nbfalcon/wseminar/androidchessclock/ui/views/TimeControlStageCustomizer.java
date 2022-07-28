package org.nbfalcon.wseminar.androidchessclock.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
    private @Nullable OnChangeListener onChangeListener = null;

    /**
     * {@link #onChangeListener} will fire only if this is true.
     * <p>
     * Used to temporarily mask the onChangeListener so that setSelected can be called without the listener firing.
     */
    private boolean onChangeListenerArmed = true;

    {
        LayoutInflater.from(getContext()).inflate(R.layout.view_time_control_stage, this);
        baseTime = findViewById(R.id.base_time);
        increment = findViewById(R.id.increment);
        increment.setMinValue(0);
        increment.setMaxValue(180); // lichess.org has the same limit
        incrementType = findViewById(R.id.incrementType);

        baseTime.setOnValueChangeListener(newTimeSeconds -> {
            fireChangeListener();
        });
        increment.setOnValueChangedListener((picker, oldVal, newVal) -> {
            fireChangeListener();
        });
        incrementType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fireChangeListener();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fireChangeListener() {
        if (onChangeListenerArmed && onChangeListener != null) onChangeListener.somethingChanged();
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

    public void bindFrom(SingleStageTimeControlTemplate timeControl) {
        onChangeListenerArmed = false;
        // FIXME: this is kinda hacky, since we're losing precision; however, since there is no other way to input time...
        baseTime.setTimeSeconds(timeControl.time / 1000);
        increment.setValue((int) (timeControl.increment / 1000));
        incrementType.setSelection(timeControl.type.ordinal());
        onChangeListenerArmed = true;
    }

    public void setOnChangeListener(@Nullable OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public interface OnChangeListener {
        // FIXME: maybe make this name a bit better
        void somethingChanged();
    }
}
