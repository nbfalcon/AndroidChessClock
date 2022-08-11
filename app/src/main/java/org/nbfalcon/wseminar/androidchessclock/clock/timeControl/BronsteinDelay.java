package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

import android.os.Parcel;
import android.os.Parcelable;

public class BronsteinDelay implements TimeControl, Parcelable {
    public static final Creator<BronsteinDelay> CREATOR = new Creator<BronsteinDelay>() {
        @Override
        public BronsteinDelay createFromParcel(Parcel in) {
            return new BronsteinDelay(in);
        }

        @Override
        public BronsteinDelay[] newArray(int size) {
            return new BronsteinDelay[size];
        }
    };
    private final long increment;
    private long timeLeft;
    private int elapsedThisMove = 0;

    public BronsteinDelay(long totalTime, long increment) {
        this.timeLeft = totalTime;
        this.increment = increment;
    }

    protected BronsteinDelay(Parcel in) {
        increment = in.readLong();
        timeLeft = in.readLong();
        elapsedThisMove = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(increment);
        dest.writeLong(timeLeft);
        dest.writeInt(elapsedThisMove);
    }

    @Override
    public int describeContents() {
        return 0;
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
