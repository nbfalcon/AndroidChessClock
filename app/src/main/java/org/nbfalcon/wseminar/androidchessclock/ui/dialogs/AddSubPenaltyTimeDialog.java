package org.nbfalcon.wseminar.androidchessclock.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimePickerWithSeconds;
import org.nbfalcon.wseminar.androidchessclock.util.Consumer;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.DialogOnce;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.ViewFlipperUtils;

// FIXME: the milliseconds get lost in the echo
public class AddSubPenaltyTimeDialog extends DialogOnce.DialogWithOnDismissBase {
    private @Nullable View rootView;
    private TabLayout whichPlayer;
    private TimePickerWithSeconds p1Time, p2Time;
    private @Nullable ClockPair bindFromMe = null;
    private Consumer<TimePair> onAccept;

    @SuppressLint("InflateParams")
    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_penalty_time, null);
            setupRootView(rootView);

            if (bindFromMe != null) {
                setViewForBind(bindFromMe);
                bindFromMe = null;
            }
        }

        return new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .setPositiveButton("Accept", (button, what) -> onAccept.accept(new TimePair(
                        p1Time.getTimeSeconds() * 1000,
                        p2Time.getTimeSeconds() * 1000)))
                .setNegativeButton("Cancel", null)
                .create();
    }

    private void setViewForBind(@NotNull ClockPair bind) {
        p1Time.setTimeSeconds(bind.getClockFor(false).getTimeLeft() / 1000);
        p2Time.setTimeSeconds(bind.getClockFor(true).getTimeLeft() / 1000);
    }

    private void setupRootView(@NotNull View rootView) {
        ViewFlipper whichPlayerTabs = rootView.findViewById(R.id.setTimeFlipper);
        whichPlayer = rootView.findViewById(R.id.playerTabs);
        ViewFlipperUtils.linkWithTabLayout(whichPlayer, whichPlayerTabs);
        p1Time = rootView.findViewById(R.id.player1SetTime);
        p2Time = rootView.findViewById(R.id.player2SetTime);

        @SuppressWarnings("unchecked") Pair<Integer, Integer>[] id2Delta = new Pair[]{
                Pair.create(R.id.sub2m, -2 * 60), // FIDE
                Pair.create(R.id.sub5s, -5),
                Pair.create(R.id.add15s, 15),
                Pair.create(R.id.add5s, 5),
        };
        for (Pair<Integer, Integer> id2Time : id2Delta) {
            rootView.findViewById(id2Time.first).setOnClickListener(new AddSubPenaltyButtonHandler(id2Time.second));
        }
    }

    public void bind(ClockPair runningClocks, Consumer<TimePair> onAcceptCB) {
        if (rootView != null) {
            setViewForBind(runningClocks);
        } else {
            bindFromMe = runningClocks;
        }
        this.onAccept = onAcceptCB;
    }

    private boolean currentPlayer() {
        return whichPlayer.getSelectedTabPosition() == 1;
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (rootView != null && rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    public static class TimePair {
        private final long p1MS, p2MS;

        public TimePair(long p1MS, long p2MS) {
            this.p1MS = p1MS;
            this.p2MS = p2MS;
        }

        public void applyTo(ClockPair theClockPair) {
            theClockPair.getClockFor(false).setTime(p1MS);
            theClockPair.getClockFor(true).setTime(p2MS);
        }
    }

    private class AddSubPenaltyButtonHandler implements View.OnClickListener {
        private final long deltaTS;

        private AddSubPenaltyButtonHandler(long deltaTS) {
            this.deltaTS = deltaTS;
        }

        @Override
        public void onClick(View v) {
            TimePickerWithSeconds whichPicker = !currentPlayer() ? p1Time : p2Time;
            whichPicker.addTime(deltaTS);
        }
    }
}
