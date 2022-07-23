package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;

public class ClockPairTemplate {
    private final @NotNull StagedPlayerClockTemplate player1;
    private final @Nullable StagedPlayerClockTemplate player2;

    public ClockPairTemplate(@NotNull StagedPlayerClockTemplate player1, @Nullable StagedPlayerClockTemplate player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public ClockPair create() {
        @NotNull StagedPlayerClockTemplate p2 = player2 == null ? player1 : player2;
        return new ClockPair(player1.createPlayerClock(), p2.createPlayerClock());
    }
}
