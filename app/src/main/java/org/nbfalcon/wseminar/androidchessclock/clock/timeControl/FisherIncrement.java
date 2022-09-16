package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

import android.os.Parcel;
import android.os.Parcelable;

public class FisherIncrement implements TimeControl, Parcelable {
    public static final Creator<FisherIncrement> CREATOR = new Creator<FisherIncrement>() {
        @Override
        public FisherIncrement createFromParcel(Parcel in) {
            return new FisherIncrement(in);
        }

        @Override
        public FisherIncrement[] newArray(int size) {
            return new FisherIncrement[size];
        }
    };
    private final long increment;
    private long timeLeft;

    public FisherIncrement(long totalTime, long increment) {
        this.timeLeft = totalTime;
        this.increment = increment;
    }

    protected FisherIncrement(Parcel in) {
        increment = in.readLong();
        timeLeft = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(increment);
        dest.writeLong(timeLeft);
    }

    @Override
    public int describeContents() {
        return 0;
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

    @Override
    public void setTime(long deltaTMS) {
        timeLeft = deltaTMS;
    }
}
