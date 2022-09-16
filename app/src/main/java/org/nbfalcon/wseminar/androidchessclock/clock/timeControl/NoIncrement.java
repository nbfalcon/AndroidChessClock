package org.nbfalcon.wseminar.androidchessclock.clock.timeControl;

import android.os.Parcel;
import android.os.Parcelable;

public class NoIncrement implements TimeControl, Parcelable {
    public static final Creator<NoIncrement> CREATOR = new Creator<NoIncrement>() {
        @Override
        public NoIncrement createFromParcel(Parcel in) {
            return new NoIncrement(in);
        }

        @Override
        public NoIncrement[] newArray(int size) {
            return new NoIncrement[size];
        }
    };
    private long time;

    public NoIncrement(long time) {
        this.time = time;
    }

    protected NoIncrement(Parcel in) {
        time = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(time);
    }

    @Override
    public int describeContents() {
        return 0;
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

    @Override
    public void setTime(long deltaTMS) {
        time = deltaTMS;
    }
}
