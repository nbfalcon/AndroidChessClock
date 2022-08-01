package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;

public class ClockPairTemplate {
    private final @NotNull String name;
    private final PlayerClockTemplate player1;
    private final @Nullable PlayerClockTemplate player2;

    public ClockPairTemplate(@NotNull String name, @NotNull PlayerClockTemplate player1, @Nullable PlayerClockTemplate player2) {
        this.name = name;
        this.player1 = player1;
        this.player2 = player2;
    }

    public ClockPair create() {
        return new ClockPair(getPlayer1().createPlayerClock(), getPlayer2().createPlayerClock());
    }

    @NotNull
    public PlayerClockTemplate getPlayer1() {
        return player1;
    }

    @NotNull
    public PlayerClockTemplate getPlayer2() {
        return player2 == null ? player1 : player2;
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return name;
    }
}
