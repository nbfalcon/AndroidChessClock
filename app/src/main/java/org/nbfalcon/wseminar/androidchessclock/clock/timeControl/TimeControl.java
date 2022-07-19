package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public interface TimeControl {
    void update(long elapsedTime);

    void moveFinished();

    long getTimeLeft();
}
