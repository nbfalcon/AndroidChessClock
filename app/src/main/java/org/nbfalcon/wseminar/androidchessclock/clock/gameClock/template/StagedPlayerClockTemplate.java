package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.StagedClock;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

import java.util.List;

public class StagedPlayerClockTemplate implements PlayerClockTemplate {
    private final List<TimeControlStageTemplate> stages;

    public StagedPlayerClockTemplate(List<TimeControlStageTemplate> stages) {
        this.stages = stages;
    }

    public TimeControl createPlayerClock() {
        if (stages.size() == 1 && stages.get(0).nMoves == -1) {
            return stages.get(0).createPlayerClock();
        }
        else {
            return new StagedClock(stages.iterator());
        }
    }
}
