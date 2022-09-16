package org.nbfalcon.wseminar.androidchessclock.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimeControlStageCustomizer;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.DialogOnce;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.ViewFlipperUtils;

// FIXME: while the dialog is running, the clock can be started; this is kinda broken
// FIXME: do something cute/smart about auto-adjusting x in '15+x' when changing the seconds spinner
public class TimeControlCustomizerDialog extends DialogOnce.DialogWithOnDismissBase {
    private OnTimeSet onResult = null;
    private boolean forPlayer;

    private View rootView;
    private TimeControlStageCustomizer stage1, stage2;
    private AppCompatCheckBox setForBothPlayers;
    private ClockPairTemplate bindFrom;
    private EditText customTimeControlName;
    private TabLayout stagesTabs;
    private View timeControlSaveAs;
    private boolean customTimeControlSaveAsClicked;

    private boolean settingWantSaveAs = true;

    @NotNull
    public ClockPairTemplate getClockPairTemplate() {
        SingleStageTimeControlTemplate p1 = new SingleStageTimeControlTemplate(getStage1OrBoth().getBaseTimeMS(), getStage1OrBoth().getIncrementMS(), getStage1OrBoth().getIncrementType());

        @NotNull String name = getTimeControlName();
        ClockPairTemplate newClockPairTemplate;
        if (shouldSetForBothPlayers()) {
            newClockPairTemplate = new ClockPairTemplate(name, p1, null);
        } else {
            SingleStageTimeControlTemplate p2 = new SingleStageTimeControlTemplate(getStage2().getBaseTimeMS(), getStage2().getIncrementMS(), getStage2().getIncrementType());
            newClockPairTemplate = new ClockPairTemplate(name, p1, p2);
        }
        return newClockPairTemplate;
    }

    public void bind(boolean forPlayer, ClockPairTemplate clocks, OnTimeSet onResult) {
        this.forPlayer = forPlayer;
        this.onResult = onResult;
        this.bindFrom = clocks;
    }

    public void setSettingWantSaveAs(boolean settingWantSaveAs) {
        this.settingWantSaveAs = settingWantSaveAs;
    }

    private void setupViews(@NotNull View from) {
        stage1 = from.findViewById(R.id.timeControlStageCustomizer1);
        stage2 = from.findViewById(R.id.timeControlStageCustomizer2);

        // FIXME: cute "(1), (2), (3)" automatic increment?
        customTimeControlName = from.findViewById(R.id.customTimeControlName);

        timeControlSaveAs = from.findViewById(R.id.customTimeControlSaveAs);
        timeControlSaveAs.setOnClickListener((v) -> {
            InputMethodService inputMethodService = ContextCompat.getSystemService(customTimeControlName.getContext(), InputMethodService.class);
            if (inputMethodService != null) {
                // Hide the keyboard so that the text the user has typed will be committed and available on the next getText()
                // call (otherwise that returns the empty string).
                inputMethodService.onFinishInput();
            }
            customTimeControlSaveAsClicked = true;
            dismiss();
            onResult.setTime(this);
        });
        if (!settingWantSaveAs) {
            timeControlSaveAs.setVisibility(View.GONE);
            timeControlSaveAs.setEnabled(false);
        }

        stagesTabs = from.findViewById(R.id.stagesTabs);
        ViewFlipper stagesFlipper = from.findViewById(R.id.stagesFlipper);
        ViewFlipperUtils.linkWithTabLayout(stagesTabs, stagesFlipper);

        setForBothPlayers = from.findViewById(R.id.setForBothPlayers);
        setForBothPlayers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TabLayout.Tab theOtherTab = stagesTabs.getTabAt((stagesTabs.getSelectedTabPosition() + 1) % 2);
            assert theOtherTab != null;
            theOtherTab.view.setEnabled(!isChecked);
        });
    }

    private void bindViews() {
        stage1.bindFrom((SingleStageTimeControlTemplate) bindFrom.getPlayer1());
        stage2.bindFrom((SingleStageTimeControlTemplate) bindFrom.getPlayer2());

        customTimeControlName.setText(bindFrom.toString());
        if (!settingWantSaveAs) {
            timeControlSaveAs.setVisibility(View.GONE);
            timeControlSaveAs.setEnabled(false);
        } else {
            timeControlSaveAs.setVisibility(View.VISIBLE);
            timeControlSaveAs.setEnabled(true);
        }

        stagesTabs.selectTab(stagesTabs.getTabAt(!forPlayer ? 0 : 1));
        setForBothPlayers.setChecked(bindFrom.setForBothPlayers());
    }

    @SuppressLint("InflateParams")
    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            // Since android.app.androidTimePickerDialog does something similar (inflating the view first, then setting stuff up),
            // we are going to do the same; This StackOverflow solution is much more hacky IMHO
            // https://stackoverflow.com/questions/17805040/how-to-create-a-number-picker-dialog
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.view_player_clock_customizer, null);
            setupViews(rootView);
        }
        bindViews();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        AlertDialog dialog = builder.setView(rootView).setPositiveButton("Accept", (dialog1, which) -> onResult.setTime(this)).setNegativeButton("Cancel", null).create();
        dialog.setOnShowListener(dialog1 -> customTimeControlSaveAsClicked = false);
        return dialog;
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // Detach view for reuse; clicking "Ok" will cause the parent to be null, however (probably due to `mViewDestroyed`)
        if (rootView != null && rootView.getParent() != null) {
            ((ViewGroup)rootView.getParent()).removeView(rootView);
        }
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
     * @return The name of the time control if the user wants to save it.
     */
    public @NotNull String getTimeControlName() {
        return customTimeControlName.getText().toString();
    }

    public HowExited getResultType() {
        return customTimeControlSaveAsClicked ? HowExited.CREATE_NEW : HowExited.OK;
    }

    public enum HowExited {
        OK, CREATE_NEW
    }

    @FunctionalInterface
    public interface OnTimeSet {
        void setTime(TimeControlCustomizerDialog dialog);
    }
}