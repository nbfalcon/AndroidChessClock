package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;
import org.nbfalcon.wseminar.androidchessclock.util.android.compat.ParcelCompatEx;

public class ClockPairTemplate implements Parcelable {
    public static final Creator<ClockPairTemplate> CREATOR = new Creator<ClockPairTemplate>() {
        @Override
        public ClockPairTemplate createFromParcel(Parcel in) {
            return readFromParcel(in);
        }

        @Override
        public ClockPairTemplate[] newArray(int size) {
            return new ClockPairTemplate[size];
        }
    };

    public static final ClockPairTemplate[] EMPTY_ARRAY = new ClockPairTemplate[0];

    private final @NotNull String name;
    private PlayerClockTemplate player1;
    private @Nullable PlayerClockTemplate player2;

    public ClockPairTemplate(@NotNull String name, @NotNull PlayerClockTemplate player1, @Nullable PlayerClockTemplate player2) {
        this.name = name;
        this.player1 = player1;
        this.player2 = player2;
    }

    private static ClockPairTemplate readFromParcel(Parcel src) {
        @NotNull String name = src.readString();
        @NotNull PlayerClockTemplate p1 = ParcelCompatEx.readParcelable(src);
        @Nullable PlayerClockTemplate p2 = ParcelCompatEx.readBoolean(src) ? ParcelCompatEx.readParcelable(src) : null;

        return new ClockPairTemplate(name, p1, p2);
    }

    public ClockPair create() {
        return new ClockPair(getPlayer1().createPlayerClock(), getPlayer2().createPlayerClock());
    }

    @NotNull
    public PlayerClockTemplate getPlayer1() {
        return player1;
    }

    @NotNull
    public PlayerClockTemplate getPlayer2() {
        return player2 == null ? player1 : player2;
    }

    public boolean setForBothPlayers() {
        return player2 == null;
    }

    public void bindFrom(@NotNull ClockPairTemplate other) {
        this.player1 = other.player1;
        this.player2 = other.player2;
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final Parcelable p1 = (Parcelable) player1, p2 = (Parcelable) player2;

        dest.writeString(name);
        ParcelCompatEx.writeParcelable(dest, p1, 0);
        ParcelCompatEx.writeBoolean(dest, p2 != null);
        if (p2 != null) {
            ParcelCompatEx.writeParcelableCreator(dest, p2);
            p2.writeToParcel(dest, flags);
        }
    }
}
