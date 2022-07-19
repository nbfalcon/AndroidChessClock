package org.nbfalcon.wseminar.androidchessclock.clock.timer;

public interface Timer {
    void onStartTimer(TimerHandler then, long initialDelayHintMS);

    void onStopTimer();

    void onRenewClock();

    default void onKillTimer() {
        onStopTimer();
    }

    @FunctionalInterface
    interface TimerHandler {
        void onTick(long elapsedMS);
    }
}
