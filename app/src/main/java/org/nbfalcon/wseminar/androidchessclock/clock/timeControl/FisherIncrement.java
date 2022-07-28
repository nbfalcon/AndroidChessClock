package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public class FisherIncrement implements TimeControl {
    private final long increment;
    private long timeLeft;

    public FisherIncrement(long totalTime, long increment) {
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
