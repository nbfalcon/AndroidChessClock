package org.nbfalcon.wseminar.androidchessclock.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimePickerWithSeconds;

public class PlayerClockCustomizerDialog extends DialogFragment {
    private final OnTimeSet onTimeSet;

    public PlayerClockCustomizerDialog(OnTimeSet onTimeSet) {
        this.onTimeSet = onTimeSet;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.view_player_clock_customizer)
                .setPositiveButton("Accept", (dialog, which) -> {
                    TimePickerWithSeconds baseTime = ((AlertDialog) dialog).findViewById(R.id.base_time);
                    AppCompatCheckBox setForBothPlayers = ((AlertDialog) dialog).findViewById(R.id.set_for_both_players);
                    onTimeSet.setTime(baseTime.getTimeSeconds() * 1000, setForBothPlayers.isChecked());
                })
                .setNegativeButton("Cancel", null);
        return builder.create();
    }

    @FunctionalInterface
    public interface OnTimeSet {
        void setTime(long timeMS, boolean forBothPlayers);
    }
}