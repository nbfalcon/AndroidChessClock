package org.nbfalcon.wseminar.androidchessclock.clock.gameClock;

import android.os.Parcel;
import android.os.Parcelable;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;

public class ClockPair implements Parcelable {
    public static final Creator<ClockPair> CREATOR = new Creator<ClockPair>() {
        @Override
        public ClockPair createFromParcel(Parcel in) {
            return new ClockPair(in);
        }

        @Override
        public ClockPair[] newArray(int size) {
            return new ClockPair[size];
        }
    };
    private final TimeControl player1;
    private final TimeControl player2;

    public ClockPair(TimeControl player1, TimeControl player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    protected ClockPair(Parcel in) {
        player1 = in.readParcelable(this.getClass().getClassLoader());
        player2 = in.readParcelable(this.getClass().getClassLoader());
    }

    public TimeControl getClockFor(boolean player) {
        return !player ? player1 : player2;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) player1, flags);
        dest.writeParcelable((Parcelable) player2, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
