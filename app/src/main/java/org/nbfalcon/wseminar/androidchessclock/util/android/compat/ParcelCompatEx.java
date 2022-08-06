package org.nbfalcon.wseminar.androidchessclock.util.android.compat;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelCompatEx {
    protected ParcelCompatEx() {
    }

    public static void writeBoolean(Parcel dest, boolean value) {
        dest.writeInt(value ? 1 : 0);
    }

    public static boolean readBoolean(Parcel src) {
        return src.readInt() != 0;
    }

    public static void writeParcelableCreator(Parcel dest, Parcelable writeMyCL) {
        dest.writeString(writeMyCL.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> Parcelable.Creator<T> readParcelableCreator(Parcel src) {
        String clName = src.readString();
        try {
            Class<?> clazz = Class.forName(clName);
            return (Parcelable.Creator<T>) clazz.getField("CREATOR").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeParcelable(Parcel dest, Parcelable writeMe, int flags) {
        ParcelCompatEx.writeParcelableCreator(dest, writeMe);
        writeMe.writeToParcel(dest, flags);
    }

    public static <T> T readParcelable(Parcel src) {
        Parcelable.Creator<T> creator = readParcelableCreator(src);
        return creator.createFromParcel(src);
    }
}
