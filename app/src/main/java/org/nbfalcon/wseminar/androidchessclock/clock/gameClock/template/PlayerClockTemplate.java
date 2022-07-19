package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.StagedClock;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

import java.util.List;

public class PlayerClockTemplate {
    private final List<TimeControlStageTemplate> stages;

    public PlayerClockTemplate(List<TimeControlStageTemplate> stages) {
        this.stages = stages;
    }

    public TimeControl create() {
        if (stages.size() == 1 && stages.get(0).nMoves == -1) {
            return stages.get(0).create().getTimeControl();
        }
        else {
            return new StagedClock(stages.iterator());
        }
    }
}
