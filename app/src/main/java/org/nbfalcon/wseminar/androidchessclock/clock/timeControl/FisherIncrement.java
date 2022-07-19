package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public class FisherIncrement implements TimeControl {
    private final int increment;
    private int timeLeft;

    public FisherIncrement(int totalTime, int increment) {
        this.timeLeft = totalTime;
        this.increment = increment;
    }

    @Override
    public void onUpdate(long elapsedTime) {
        timeLeft -= elapsedTime;
    }

    @Override
    public void onMoveFinished() {
        timeLeft += increment;
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }
}
