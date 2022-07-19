package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

public interface TimeControl {
    void onUpdate(long elapsedTime);

    void onMoveFinished();

    long getTimeLeft();
}
