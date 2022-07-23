package org.nbfalcon.wseminar.androidchessclock.clock.gameClock;

import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.PlayerClockTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;

public class BuiltinTimeControls {
    public static final PlayerClockTemplate[] BUILTIN = new PlayerClockTemplate[]{new SingleStageTimeControlTemplate("15+0", 15 * 60 * 1000, 0, TimeControlStageTemplate.Type.FISHER), new SingleStageTimeControlTemplate("5+0", 5 * 60 * 1000, 0, TimeControlStageTemplate.Type.FISHER), new SingleStageTimeControlTemplate("2+1", 2 * 60 * 1000, 1000, TimeControlStageTemplate.Type.FISHER)};

    protected BuiltinTimeControls() {
    }
}
