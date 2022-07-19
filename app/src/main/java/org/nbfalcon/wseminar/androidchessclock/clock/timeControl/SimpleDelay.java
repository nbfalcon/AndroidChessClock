package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public class SimpleDelay implements TimeControl {
    private final int increment;
    private int timeLeft;

    private int elapsedThisMove = 0;

    public SimpleDelay(int totalTime, int increment) {
        this.timeLeft = totalTime;
        this.increment = increment;
    }

    @Override
    public void update(long elapsedTime) {
        // If the increment is elapsed, we start subtracting from the time
        elapsedThisMove += elapsedTime;
        if (elapsedThisMove > increment) {
            timeLeft -= (elapsedThisMove - increment);
            elapsedThisMove = increment;
        }
    }

    @Override
    public void moveFinished() {
        elapsedThisMove = 0;
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }
}
