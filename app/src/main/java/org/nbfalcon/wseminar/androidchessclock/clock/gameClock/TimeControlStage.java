package org.nbfalcon.wseminar.androidchessclock.clock.gameClock;

import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

public class TimeControlStage {
    public final @NotNull TimeControl timeControl;
    private int nMovesLeft;

    public TimeControlStage(@NotNull TimeControl timeControl, int nMovesLeft) {
        this.nMovesLeft = nMovesLeft;
        this.timeControl = timeControl;
    }

    public void onMoveFinished() {
        assert nMovesLeft == -1 || nMovesLeft > 0;

        if (nMovesLeft != -1) nMovesLeft--;
    }

    public @NotNull TimeControl getTimeControl() {
        return this.timeControl;
    }

    public boolean areMovesUp() {
        return nMovesLeft == 0;
    }
}
