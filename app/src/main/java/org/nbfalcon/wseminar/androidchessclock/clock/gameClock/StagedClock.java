package org.nbfalcon.wseminar.androidchessclock.clock.gameClock;

import androidx.annotation.Nullable;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

import java.util.Iterator;

public class StagedClock implements TimeControl {
    private final Iterator<TimeControlStageTemplate> nextStages;
    private @Nullable TimeControlStage current;

    public StagedClock(Iterator<TimeControlStageTemplate> nextStages) {
        this.nextStages = nextStages;
        current = nextStages.next().createTimeControlStage();
    }

    @Override
    public void onUpdate(long elapsedTime) {
        if (current != null) {
            current.getTimeControl().onUpdate(elapsedTime);
        }
    }

    @Override
    public void onMoveFinished() {
        if (current != null) {
            current.onMoveFinished();
            if (current.areMovesUp()) {
                current = nextStages.hasNext() ? nextStages.next().createTimeControlStage() : null;
            }
        }
    }

    @Override
    public long getTimeLeft() {
        if (current == null) return 0;
        return current.getTimeControl().getTimeLeft();
    }
}
