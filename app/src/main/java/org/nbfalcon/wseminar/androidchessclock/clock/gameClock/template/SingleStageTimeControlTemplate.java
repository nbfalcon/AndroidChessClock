package org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template;

import android.os.Parcel;
import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.*;

public class SingleStageTimeControlTemplate implements PlayerClockTemplate, Parcelable {
    public static final Creator<SingleStageTimeControlTemplate> CREATOR = new Creator<SingleStageTimeControlTemplate>() {
        @Override
        public SingleStageTimeControlTemplate createFromParcel(Parcel in) {
            return new SingleStageTimeControlTemplate(in);
        }

        @Override
        public SingleStageTimeControlTemplate[] newArray(int size) {
            return new SingleStageTimeControlTemplate[size];
        }
    };

    public final @NotNull TimeControlStageTemplate.Type type;
    public final long time;
    public final long increment;

    public SingleStageTimeControlTemplate(long time, long increment, @NotNull TimeControlStageTemplate.Type type) {
        this.time = time;
        this.increment = increment;
        this.type = type;
    }

    protected SingleStageTimeControlTemplate(Parcel in) {
        this.type = TimeControlStageTemplate.Type.values()[in.readInt()];
        this.time = in.readLong();
        this.increment = in.readLong();
    }

    @Override
    public TimeControl createPlayerClock() {
        TimeControl timeControl;
        switch (this.type) {
            case NONE:
                timeControl = new NoIncrement(time);
                break;
            case BRONSTEIN:
                timeControl = new BronsteinDelay(time, increment);
                break;
            case FISHER:
                timeControl = new FisherIncrement(time, increment);
                break;
            case SIMPLE:
                timeControl = new SimpleDelay(time, increment);
                break;
            default:
                throw new AssertionError("[type] is invalid");
        }
        return timeControl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type.ordinal());
        dest.writeLong(this.time);
        dest.writeLong(this.increment);
    }
}
