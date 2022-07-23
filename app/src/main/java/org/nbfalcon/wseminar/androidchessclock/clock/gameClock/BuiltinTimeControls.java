package org.nbfalcon.wseminar.androidchessclock.clock.gameClock;

import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.PlayerClockTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;

public class BuiltinTimeControls {
    public final PlayerClockTemplate[] BUILTIN = new PlayerClockTemplate[]{new SingleStageTimeControlTemplate(TimeControlStageTemplate.Type.FISHER, 5 * 300 * 1000, 0), new SingleStageTimeControlTemplate(TimeControlStageTemplate.Type.FISHER, 300 * 1000, 0), new SingleStageTimeControlTemplate(TimeControlStageTemplate.Type.FISHER, 120 * 1000, 1000)};

    protected BuiltinTimeControls() {
    }
}
