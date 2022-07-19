package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public class BronsteinDelay implements TimeControl {
    private final int increment;
    private int timeLeft;

    private int elapsedThisMove = 0;

    public BronsteinDelay(int totalTime, int increment) {
        this.timeLeft = totalTime;
        this.increment = increment;
    }

    @Override
    public void onUpdate(long elapsedTime) {
        timeLeft -= elapsedTime;
        elapsedThisMove += elapsedTime;
    }

    @Override
    public void onMoveFinished() {
        elapsedThisMove = 0;
        timeLeft += Math.min(increment, elapsedThisMove);
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }
}
