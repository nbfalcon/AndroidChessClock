package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.BronsteinDelay;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.FisherIncrement;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.SimpleDelay;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

public class SingleStageTimeControlTemplate implements PlayerClockTemplate {
    public final @NotNull String name;
    public final @NotNull TimeControlStageTemplate.Type type;
    public final int time;
    public final int increment;

    public SingleStageTimeControlTemplate(@NotNull String name, int time, int increment, @NotNull TimeControlStageTemplate.Type type) {
        this.name = name;
        this.time = time;
        this.increment = increment;
        this.type = type;
    }

    @Override
    public TimeControl createPlayerClock() {
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
        return timeControl;
    }

    public enum Type {
        BRONSTEIN, FISHER, SIMPLE
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return name;
    }
}
