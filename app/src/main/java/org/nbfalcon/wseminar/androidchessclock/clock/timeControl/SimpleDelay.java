package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public class SimpleDelay implements TimeControl {
    private final long increment;
    private long timeLeft;

    private long elapsedThisMove = 0;

    public SimpleDelay(long totalTime, long increment) {
        this.timeLeft = totalTime;
        this.increment = increment;
    }

    @Override
    public void onUpdate(long elapsedTime) {
        // If the increment is elapsed, we start subtracting from the time
        elapsedThisMove += elapsedTime;
        if (elapsedThisMove > increment) {
            timeLeft -= (elapsedThisMove - increment);
            elapsedThisMove = increment;
        }
    }

    @Override
    public void onMoveFinished() {
        elapsedThisMove = 0;
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }
}
