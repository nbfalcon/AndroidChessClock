package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

import android.os.Parcel;
import android.os.Parcelable;

public class SimpleDelay implements TimeControl, Parcelable {
    public static final Creator<SimpleDelay> CREATOR = new Creator<SimpleDelay>() {
        @Override
        public SimpleDelay createFromParcel(Parcel in) {
            return new SimpleDelay(in);
        }

        @Override
        public SimpleDelay[] newArray(int size) {
            return new SimpleDelay[size];
        }
    };
    private final long increment;
    private long timeLeft;
    private long elapsedThisMove = 0;

    public SimpleDelay(long totalTime, long increment) {
        this.timeLeft = totalTime;
        this.increment = increment;
    }

    protected SimpleDelay(Parcel in) {
        increment = in.readLong();
        timeLeft = in.readLong();
        elapsedThisMove = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(increment);
        dest.writeLong(timeLeft);
        dest.writeLong(elapsedThisMove);
    }

    @Override
    public int describeContents() {
        return 0;
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
