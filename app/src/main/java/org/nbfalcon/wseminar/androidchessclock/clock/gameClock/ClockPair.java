package org.nbfalcon.wseminar.androidchessclock.clock.gameClock;

import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

public class ClockPair {
    private final TimeControl player1;
    private final TimeControl player2;

    public ClockPair(TimeControl player1, TimeControl player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public TimeControl getClockFor(boolean player) {
        return !player ? player1 : player2;
    }
}
