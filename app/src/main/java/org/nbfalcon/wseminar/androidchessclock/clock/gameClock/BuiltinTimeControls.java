package org.nbfalcon.wseminar.androidchessclock.clock.gameClock;

import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;

public class BuiltinTimeControls {
    public static final ClockPairTemplate[] BUILTIN = new ClockPairTemplate[]{
            builtin("15+0", 15 * 60, 0),
            builtin("5+0", 5 * 60, 0),
            builtin("2+1", 2 * 60, 1)};

    protected BuiltinTimeControls() {
    }

    private static ClockPairTemplate builtin(@NotNull String name, long timeS, long incrementS) {
        SingleStageTimeControlTemplate onePlayer = new SingleStageTimeControlTemplate(
                timeS * 1000L, incrementS * 1000L, TimeControlStageTemplate.Type.FISHER);
        return new ClockPairTemplate(name, onePlayer, null);
    }
}
