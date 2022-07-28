package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public class NoIncrement implements TimeControl {
    private long time;

    public NoIncrement(long time) {
        this.time = time;
    }

    @Override
    public void onUpdate(long elapsedTime) {
        time -= elapsedTime;
    }

    @Override
    public void onMoveFinished() {
    }

    @Override
    public long getTimeLeft() {
        return time;
    }
}
