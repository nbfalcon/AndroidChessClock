package org.nbfalcon.wseminar.androidchessclock.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimePickerWithSeconds;

public class PlayerClockCustomizerDialog extends DialogFragment {
    private final OnTimeSet onTimeSet;
    private TimePickerWithSeconds baseTime;
    private AppCompatCheckBox setForBothPlayers;
    private NumberPicker increment;
    private Spinner incrementType;

    public PlayerClockCustomizerDialog(OnTimeSet onTimeSet) {
        this.onTimeSet = onTimeSet;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Since android.app.androidTimePickerDialog does something similar (inflating the view first, then setting stuff up),
        // we are going to do the same; This StackOverflow solution is much more hacky IMHO
        // https://stackoverflow.com/questions/17805040/how-to-create-a-number-picker-dialog
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_player_clock_customizer, null);

        baseTime = view.findViewById(R.id.base_time);
        setForBothPlayers = view.findViewById(R.id.set_for_both_players);
        increment = view.findViewById(R.id.increment);
        increment.setMinValue(0);
        increment.setMaxValue(180); // lichess.org has the same limit
        incrementType = view.findViewById(R.id.incrementType);

        return builder.setView(view)
                .setPositiveButton("Accept", (dialog, which) -> onTimeSet.setTime(this))
                .setNegativeButton("Cancel", null)
                .create();
    }


    public long getIncrementMS() {
        return increment.getValue();
    }

    public long getBaseTimeMS() {
        return baseTime.getTimeSeconds() * 1000;
    }

    public boolean shouldSetForBothPlayers() {
        return setForBothPlayers.isChecked();
    }

    public TimeControlStageTemplate.Type getIncrementType() {
        return TimeControlStageTemplate.Type.values()[(int) incrementType.getSelectedItemId()];
    }
    @FunctionalInterface
    public interface OnTimeSet {
        void setTime(PlayerClockCustomizerDialog dialog);
    }
}