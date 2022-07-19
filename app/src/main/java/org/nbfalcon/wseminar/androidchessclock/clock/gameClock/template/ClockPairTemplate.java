package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;

public class ClockPairTemplate {
    private final @NotNull PlayerClockTemplate player1;
    private final @Nullable PlayerClockTemplate player2;

    public ClockPairTemplate(@NotNull PlayerClockTemplate player1, @Nullable PlayerClockTemplate player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public ClockPair create() {
        @NotNull PlayerClockTemplate p2 = player2 == null ? player1 : player2;
        return new ClockPair(player1.create(), p2.create());
    }
}
