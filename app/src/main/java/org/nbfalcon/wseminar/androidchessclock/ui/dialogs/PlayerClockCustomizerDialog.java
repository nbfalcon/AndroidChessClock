package org.nbfalcon.wseminar.androidchessclock.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimeControlStageCustomizer;
import org.nbfalcon.wseminar.androidchessclock.util.android.ViewFlipperUtils;

public class PlayerClockCustomizerDialog extends DialogFragment {
    private final @NotNull OnTimeSet onTimeSet;
    private final boolean forPlayer;
    private TimeControlStageCustomizer stage1, stage2;
    private AppCompatCheckBox setForBothPlayers;
    private ClockPairTemplate bindFrom;

    public PlayerClockCustomizerDialog(boolean forPlayer, @NotNull OnTimeSet onTimeSet) {
        this.forPlayer = forPlayer;
        this.onTimeSet = onTimeSet;
    }

    public void bindFrom(ClockPairTemplate clocks) {
        this.bindFrom = clocks;
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

        // FIXME: this should probably go in onCreateView
        stage1 = view.findViewById(R.id.timeControlStageCustomizer1);
        stage2 = view.findViewById(R.id.timeControlStageCustomizer2);
        stage1.bindFrom((SingleStageTimeControlTemplate) this.bindFrom.getPlayer1());
        stage2.bindFrom((SingleStageTimeControlTemplate) this.bindFrom.getPlayer2());

        TabLayout stagesTabs = view.findViewById(R.id.stagesTabs);
        ViewFlipper stagesFlipper = view.findViewById(R.id.stagesFlipper);
        ViewFlipperUtils.linkWithTabLayout(stagesTabs, stagesFlipper);

        stagesTabs.selectTab(stagesTabs.getTabAt(!forPlayer ? 0 : 1));

        setForBothPlayers = view.findViewById(R.id.set_for_both_players);
        setForBothPlayers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TabLayout.Tab theOtherTab = stagesTabs.getTabAt((stagesTabs.getSelectedTabPosition() + 1) % 2);
            assert theOtherTab != null;
            theOtherTab.view.setEnabled(!isChecked);
        });

        return builder.setView(view)
                .setPositiveButton("Accept", (dialog, which) -> onTimeSet.setTime(this))
                .setNegativeButton("Cancel", null)
                .create();
    }

    public TimeControlStageCustomizer getStage1OrBoth() {
        if (shouldSetForBothPlayers()) {
            return !forPlayer ? stage1 : stage2;
        }
        else {
            return stage1;
        }
    }

    public TimeControlStageCustomizer getStage2() {
        return stage2;
    }

    public boolean shouldSetForBothPlayers() {
        return setForBothPlayers.isChecked();
    }

    @FunctionalInterface
    public interface OnTimeSet {
        void setTime(PlayerClockCustomizerDialog dialog);
    }
}