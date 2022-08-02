package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.PlayerClockCustomizerDialog;

public class ClockPairTemplate {
    private final @NotNull String name;
    private PlayerClockTemplate player1;
    private @Nullable PlayerClockTemplate player2;

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

    public boolean setForBothPlayers() {
        return player2 == null;
    }

    public void bindFrom(@NotNull ClockPairTemplate other) {
        this.player1 = other.player1;
        this.player2 = other.player2;
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return name;
    }
}
