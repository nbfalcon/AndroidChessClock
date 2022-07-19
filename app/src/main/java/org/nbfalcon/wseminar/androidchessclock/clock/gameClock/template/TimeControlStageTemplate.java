package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.TimeControlStage;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.BronsteinDelay;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.FisherIncrement;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.SimpleDelay;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

public class TimeControlStageTemplate {
    public final @NotNull Type type;
    public final int time;
    public final int increment;
    public final int nMoves;

    public TimeControlStageTemplate(@NotNull Type type, int time, int increment, int nMoves) {
        this.type = type;
        this.time = time;
        this.increment = increment;
        this.nMoves = nMoves;
    }

    public TimeControlStage create() {
        TimeControl timeControl;
        switch (this.type) {
            case BRONSTEIN:
                timeControl = new BronsteinDelay(time, increment);
                break;
            case FISHER:
                timeControl = new FisherIncrement(time, increment);
                break;
            case SIMPLE:
                timeControl = new SimpleDelay(time, increment);
                break;
            default:
                throw new AssertionError("[type] is invalid");
        }
        return new TimeControlStage(timeControl, nMoves);
    }

    public enum Type {
        BRONSTEIN, FISHER, SIMPLE
    }
}
