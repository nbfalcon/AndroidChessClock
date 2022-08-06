package org.nbfalcon.wseminar.androidchessclock.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimeControlStageCustomizer;
import org.nbfalcon.wseminar.androidchessclock.util.android.ViewFlipperUtils;

// FIXME: while the dialog is running, the clock can be started; this is kinda broken
public class PlayerClockCustomizerDialog extends DialogFragment {
    private final @NotNull OnTimeSet onTimeSet;
    private final boolean forPlayer;
    private TimeControlStageCustomizer stage1, stage2;
    // FIXME: default true
    private AppCompatCheckBox setForBothPlayers;
    private ClockPairTemplate bindFrom;
    // FIXME: investigate material textedit + button
    private EditText customTimeControlName;
    private boolean customTimeControlSaveAsClicked;
    private TabLayout stagesTabs;

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
        stage1.bindFrom((SingleStageTimeControlTemplate) bindFrom.getPlayer1());
        stage2.bindFrom((SingleStageTimeControlTemplate) bindFrom.getPlayer2());

        customTimeControlName = view.findViewById(R.id.customTimeControlName);
        // FIXME: cute "(1), (2), (3)" automatic increment?
        customTimeControlName.setText(bindFrom.toString());
        customTimeControlSaveAsClicked = false;
        View timeControlSaveAs = view.findViewById(R.id.customTimeControlSaveAs);
        timeControlSaveAs.setOnClickListener((v) -> {
            InputMethodService inputMethodService = ContextCompat.getSystemService(customTimeControlName.getContext(), InputMethodService.class);
            if (inputMethodService != null) {
                // Hide the keyboard so that the text the user has typed will be committed and available on the next getText()
                // call (otherwise that returns the empty string).
                inputMethodService.onFinishInput();
            }

            customTimeControlSaveAsClicked = true;
            dismiss();
            onTimeSet.setTime(this);
        });

        stagesTabs = view.findViewById(R.id.stagesTabs);
        ViewFlipper stagesFlipper = view.findViewById(R.id.stagesFlipper);

        ViewFlipperUtils.linkWithTabLayout(stagesTabs, stagesFlipper);
        stagesTabs.selectTab(stagesTabs.getTabAt(!forPlayer ? 0 : 1));
        setForBothPlayers = view.findViewById(R.id.set_for_both_players);
        setForBothPlayers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TabLayout.Tab theOtherTab = stagesTabs.getTabAt((stagesTabs.getSelectedTabPosition() + 1) % 2);
            assert theOtherTab != null;
            theOtherTab.view.setEnabled(!isChecked);
        });
        setForBothPlayers.setChecked(bindFrom.setForBothPlayers());

        return builder.setView(view)
                .setPositiveButton("Accept", (dialog, which) -> onTimeSet.setTime(this))
                .setNegativeButton("Cancel", null)
                .create();
    }

    public TimeControlStageCustomizer getStage1OrBoth() {
        if (shouldSetForBothPlayers()) {
            int selected = stagesTabs.getSelectedTabPosition();
            assert selected != -1;
            return selected == 0 ? stage1 : stage2;
        } else {
            return stage1;
        }
    }

    public TimeControlStageCustomizer getStage2() {
        return stage2;
    }

    public boolean shouldSetForBothPlayers() {
        return setForBothPlayers.isChecked();
    }

    /**
     * @return The name of the time control if the user wants to save it, otherwise null.
     */
    public @Nullable String getCustomTimeControlName() {
        return customTimeControlSaveAsClicked ? customTimeControlName.getText().toString() : null;
    }

    @FunctionalInterface
    public interface OnTimeSet {
        void setTime(PlayerClockCustomizerDialog dialog);
    }
}