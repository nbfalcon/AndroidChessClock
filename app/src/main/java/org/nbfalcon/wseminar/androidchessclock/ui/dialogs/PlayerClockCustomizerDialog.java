package org.nbfalcon.wseminar.androidchessclock.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimeControlStageCustomizer;

public class PlayerClockCustomizerDialog extends DialogFragment {
    private final OnTimeSet onTimeSet;
    private TimeControlStageCustomizer stage;
    private AppCompatCheckBox setForBothPlayers;

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

        stage = view.findViewById(R.id.timeControlStageCustomizer);
        setForBothPlayers = view.findViewById(R.id.set_for_both_players);

        return builder.setView(view)
                .setPositiveButton("Accept", (dialog, which) -> onTimeSet.setTime(this))
                .setNegativeButton("Cancel", null)
                .create();
    }

    public TimeControlStageCustomizer getStage() {
        return stage;
    }

    public boolean shouldSetForBothPlayers() {
        return setForBothPlayers.isChecked();
    }

    @FunctionalInterface
    public interface OnTimeSet {
        void setTime(PlayerClockCustomizerDialog dialog);
    }
}