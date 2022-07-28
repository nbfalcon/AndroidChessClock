package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.*;

public class SingleStageTimeControlTemplate implements PlayerClockTemplate {
    public final @NotNull String name;
    public final @NotNull TimeControlStageTemplate.Type type;
    public final long time;
    public final long increment;

    public SingleStageTimeControlTemplate(@NotNull String name, long time, long increment, @NotNull TimeControlStageTemplate.Type type) {
        this.name = name;
        this.time = time;
        this.increment = increment;
        this.type = type;
    }

    @Override
    public TimeControl createPlayerClock() {
        TimeControl timeControl;
        switch (this.type) {
            case NONE:
                timeControl = new NoIncrement(time);
                break;
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

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return name;
    }
}
