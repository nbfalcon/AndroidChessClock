package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.TimeControlStage;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.*;

public class TimeControlStageTemplate extends SingleStageTimeControlTemplate {
    public final int nMoves;

    public TimeControlStageTemplate(@NotNull Type type, int time, int increment, int nMoves) {
        super(time, increment, type);
        this.nMoves = nMoves;
    }

    public TimeControlStage createTimeControlStage() {
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
