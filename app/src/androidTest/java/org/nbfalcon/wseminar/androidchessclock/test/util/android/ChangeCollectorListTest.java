package org.nbfalcon.wseminar.androidchessclock.test.util.android;

import android.os.Parcel;
import org.junit.Assert;
import org.junit.Test;
import org.nbfalcon.wseminar.androidchessclock.testUtil.ParcelableInteger;
import org.nbfalcon.wseminar.androidchessclock.util.collections.android.ChangeCollectorList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// androidTest because I didn't want to bother with mocking Parcel
public class ChangeCollectorListTest {
    @Test
    public void doesParcellingEvenWork() {
        Parcel parcel1 = Parcel.obtain();
        parcel1.writeInt(10);
        parcel1.setDataPosition(0);
        Assert.assertEquals(10, parcel1.readInt());

        Parcel parcel2 = Parcel.obtain();
        parcel2.writeString("Meow");
        parcel2.setDataPosition(0);
        Assert.assertEquals("Meow", parcel2.readString());
        Assert.assertNull(parcel2.readString());
    }

    @Test
    public void replayWorks() {
        List<ParcelableInteger> testList = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .map(ParcelableInteger::new).collect(Collectors.toCollection(ArrayList::new));

        // testList with all random changes applied
        ArrayList<ParcelableInteger> testListMirror = new ArrayList<>(testList);

        // the recorded changes
        ChangeCollectorList<ParcelableInteger> testListChangeRec = new ChangeCollectorList<ParcelableInteger>(testList, ParcelableInteger.class);
        Random rng = new Random();
        for (int i = 0; i < 100; i++) {
            switch (rng.nextInt(4)) {
                case 0:
                    ParcelableInteger valueAdd = new ParcelableInteger(rng.nextInt());
                    testListChangeRec.add(valueAdd);
                    testListMirror.add(valueAdd);
                    break;
                case 1:
                    int indexAddAt = rng.nextInt(testListChangeRec.size());
                    ParcelableInteger valueAddAt = new ParcelableInteger(rng.nextInt());
                    testListChangeRec.add(indexAddAt, valueAddAt);
                    testListMirror.add(indexAddAt, valueAddAt);
                    break;
                case 2:
                    int indexUpdate = rng.nextInt(testListChangeRec.size());
                    ParcelableInteger valueUpdate = new ParcelableInteger(rng.nextInt());
                    testListChangeRec.set(indexUpdate, valueUpdate);
                    testListMirror.set(indexUpdate, valueUpdate);
                    break;
                case 3:
                    if (testListChangeRec.size() > 0) {
                        int indexRemove = rng.nextInt(testListChangeRec.size() - 1);
                        testListChangeRec.remove(indexRemove);
                        testListMirror.remove(indexRemove);
                    }
                    break;
            }
        }

        ArrayList<ParcelableInteger> testListApplyWithoutParcel = new ArrayList<>(testList);
        testListChangeRec.getChangeList().applyTo(testListApplyWithoutParcel);
        Assert.assertEquals("Change replay did not yield identical results (without parcelling and unparcelling first)", testListMirror, testListApplyWithoutParcel);

        Parcel parcel = Parcel.obtain();
        parcel.writeTypedObject(testListChangeRec.getChangeList(), 0);

        parcel.setDataPosition(0);
        ChangeCollectorList.ChangeList<ParcelableInteger> parcelledChangeList = parcel.readTypedObject(ChangeCollectorList.ChangeList.CREATOR).safeCast(ParcelableInteger.class);
        ArrayList<ParcelableInteger> testListApplyWithParcel = new ArrayList<>(testList);
        parcelledChangeList.applyTo(testListApplyWithParcel);
        Assert.assertEquals("Change replay did not yield identical results (with parcelling first)", testListMirror, testListApplyWithParcel);
    }
}