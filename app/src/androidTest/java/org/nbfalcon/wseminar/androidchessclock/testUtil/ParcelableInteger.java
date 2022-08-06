package org.nbfalcon.wseminar.androidchessclock.testUtil;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

public class ParcelableInteger implements Parcelable {
    public static final Creator<ParcelableInteger> CREATOR = new Creator<ParcelableInteger>() {
        @Override
        public ParcelableInteger createFromParcel(Parcel source) {
            return new ParcelableInteger(source.readInt());
        }

        @Override
        public ParcelableInteger[] newArray(int size) {
            return new ParcelableInteger[0];
        }
    };
    public final int value;

    public ParcelableInteger(int value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {
        return obj != null && getClass().equals(obj.getClass()) && value == ((ParcelableInteger) obj).value;
    }
}
