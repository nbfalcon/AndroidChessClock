package org.nbfalcon.wseminar.androidchessclock.util.collections;

import android.os.Parcel;
import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.util.CollectionUtilsEx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChangeCollectorList<E extends Parcelable> implements SimpleMutableList<E>, Parcelable {
    public static final Creator<ChangeCollectorList<?>> CREATOR = new Creator<ChangeCollectorList<?>>() {
        @Override
        public ChangeCollectorList<?> createFromParcel(Parcel in) {
            return new ChangeCollectorList<>(in);
        }

        @Override
        public ChangeCollectorList<?>[] newArray(int size) {
            return new ChangeCollectorList[size];
        }
    };

    private final ArrayList<E> items;
    private final ChangeList<E> changeList;

    public ChangeCollectorList(Collection<E> src, Class<E> parcelClass) {
        this.items = new ArrayList<>(src);
        this.changeList = new ChangeList<>(new ArrayList<>(), parcelClass);
    }

    @SuppressWarnings("unchecked")
    protected ChangeCollectorList(Parcel in) {
        changeList = in.readParcelable(this.getClass().getClassLoader());
        try {
            items = in.createTypedArrayList((Parcelable.Creator<E>) changeList.parcelClass.getField("CREATOR").get(null));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(changeList, flags);
        dest.writeTypedList(items);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void add(E item) {
        items.add(item);
        changeList.changes.add(new AddChange<>(item));
    }

    public void add(int index, E item) {
        items.add(item);
        changeList.changes.add(new InsertChange<>(item, index));
    }

    @Override
    public void remove(int index) {
        items.remove(index);
        changeList.changes.add(new RemoveChange<>(index));
    }

    @Override
    public void set(int index, E newValue) {
        items.set(index, newValue);
        changeList.changes.add(new UpdateChange<>(newValue, index));
    }

    @Override
    public E get(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public void clear() {
        items.clear();
        changeList.changes.clear();
        changeList.changes.add(ClearChange.get());
    }

    public ChangeList<E> getChangeList() {
        return changeList;
    }

    @Override
    public void move(int from, int to) {
        CollectionUtilsEx.move(items, from, to);
        Change<E> lastChange;
        // Optimize for the repeated drag case
        if (!changeList.changes.isEmpty() && (lastChange = changeList.changes.get(changeList.changes.size() - 1)) instanceof MoveChange && ((MoveChange<E>) lastChange).to == from) {
            ((MoveChange<E>) lastChange).to = to;
        } else {
            changeList.changes.add(new MoveChange<>(from, to));
        }
    }

    @Override
    public E[] toArray(E[] a) {
        return items.toArray(a);
    }

    private interface Change<E> {

        static <E> Change<E> readFromParcel(Parcel in, @NotNull Parcelable.Creator<E> elementReader) {
            Change<E> change;
            int tag = in.readInt();
            switch (Change.ChangeKind.values()[tag]) {
                case APPEND:
                    E itemA = elementReader.createFromParcel(in);
                    change = new AddChange<>(itemA);
                    break;
                case INSERT:
                    int indexI = in.readInt();
                    E itemI = elementReader.createFromParcel(in);
                    change = new InsertChange<>(itemI, indexI);
                    break;
                case REMOVE:
                    int indexR = in.readInt();
                    change = new RemoveChange<>(indexR);
                    break;
                case UPDATE:
                    int indexU = in.readInt();
                    E itemU = elementReader.createFromParcel(in);
                    change = new UpdateChange<>(itemU, indexU);
                    break;
                case MOVE:
                    int fromM = in.readInt();
                    int toM = in.readInt();
                    change = new MoveChange<>(fromM, toM);
                    break;
                case CLEAR:
                    change = ClearChange.get();
                    break;
                default:
                    throw new RuntimeException("Unknown tag " + tag);
            }
            return change;
        }

        // Don't make this a method, so that the serialization format is in one place
        static <E extends Parcelable> void writeToParcel(Change<E> change, Parcel dest, int flags) {
            if (change instanceof AddChange) {
                dest.writeInt(ChangeKind.APPEND.ordinal());
                ((AddChange<E>) change).item.writeToParcel(dest, flags);
            } else if (change instanceof InsertChange) {
                dest.writeInt(ChangeKind.INSERT.ordinal());
                dest.writeInt(((InsertChange<E>) change).index);
                ((InsertChange<E>) change).item.writeToParcel(dest, flags);
            } else if (change instanceof RemoveChange) {
                dest.writeInt(ChangeKind.REMOVE.ordinal());
                dest.writeInt(((RemoveChange<E>) change).index);
            } else if (change instanceof UpdateChange) {
                dest.writeInt(ChangeKind.UPDATE.ordinal());
                dest.writeInt(((UpdateChange<E>) change).index);
                ((UpdateChange<E>) change).item.writeToParcel(dest, flags);
            } else if (change instanceof MoveChange) {
                dest.writeInt(ChangeKind.MOVE.ordinal());
                dest.writeInt(((MoveChange<E>) change).from);
                dest.writeInt(((MoveChange<E>) change).to);
            } else if (change instanceof ClearChange) {
                dest.writeInt(ChangeKind.CLEAR.ordinal());
            }
        }

        void applyTo(List<E> applyTo);

        void applyTo(SimpleMutableList<E> applyTo);

        enum ChangeKind {
            APPEND, INSERT, REMOVE, UPDATE, MOVE, CLEAR
        }
    }

    public static final class ChangeList<E extends Parcelable> implements Parcelable {
        public static final Creator<ChangeList<?>> CREATOR = new Creator<ChangeList<?>>() {
            @Override
            public ChangeList<?> createFromParcel(Parcel in) {
                return new ChangeList<>(in);
            }

            @Override
            public ChangeList<?>[] newArray(int size) {
                return new ChangeList<?>[size];
            }
        };

        private final ArrayList<Change<E>> changes;
        private final Class<E> parcelClass;


        ChangeList(ArrayList<Change<E>> changes, Class<E> parcelClass) {
            this.changes = changes;
            this.parcelClass = parcelClass;
        }

        @SuppressWarnings("unchecked")
        private ChangeList(Parcel in) {
            try {
                parcelClass = (Class<E>) Class.forName(in.readString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            this.changes = readChangeList(in);
        }

        /**
         * Only use to make a ChangeList<?> typesafe.
         */
        @SuppressWarnings("unchecked")
        public <T extends Parcelable> ChangeList<T> safeCast(Class<T> clazz) {
            if (!this.parcelClass.equals(clazz)) throw new RuntimeException("changeList has incorrect type");
            return (ChangeList<T>) this;
        }

        @SuppressWarnings("unchecked")
        private ArrayList<Change<E>> readChangeList(Parcel in) {
            Creator<E> elementReader;
            try {
                elementReader = (Creator<E>) parcelClass.getField("CREATOR").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            assert elementReader != null;

            ArrayList<Change<E>> result = new ArrayList<>();
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                result.add(Change.readFromParcel(in, elementReader));
            }
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(parcelClass.getName());
            dest.writeInt(changes.size());
            for (Change<E> change : changes) {
                Change.writeToParcel(change, dest, flags);
            }
        }

        public void applyTo(List<E> applyTo) {
            for (Change<E> change : changes) {
                change.applyTo(applyTo);
            }
        }

        public void applyTo(SimpleMutableList<E> applyTo) {
            for (Change<E> change : changes) {
                change.applyTo(applyTo);
            }
        }
    }

    private static class AddChange<E> implements Change<E> {
        private final E item;

        public AddChange(E item) {
            this.item = item;
        }

        @Override
        public void applyTo(List<E> applyTo) {
            applyTo.add(item);
        }

        @Override
        public void applyTo(SimpleMutableList<E> applyTo) {
            applyTo.add(item);
        }
    }

    private static class InsertChange<E> implements Change<E> {
        private final E item;
        private final int index;

        public InsertChange(E item, int index) {
            this.item = item;
            this.index = index;
        }

        @Override
        public void applyTo(List<E> applyTo) {
            applyTo.add(index, item);
        }

        @Override
        public void applyTo(SimpleMutableList<E> applyTo) {
            applyTo.add(index, item);
        }
    }

    private static class UpdateChange<E> implements Change<E> {
        private final E item;
        private final int index;

        public UpdateChange(E item, int index) {
            this.item = item;
            this.index = index;
        }

        @Override
        public void applyTo(List<E> applyTo) {
            applyTo.set(index, item);
        }

        @Override
        public void applyTo(SimpleMutableList<E> applyTo) {
            applyTo.set(index, item);
        }
    }

    private static class RemoveChange<E> implements Change<E> {
        private final int index;

        public RemoveChange(int index) {
            this.index = index;
        }

        @Override
        public void applyTo(List<E> applyTo) {
            applyTo.remove(index);
        }

        @Override
        public void applyTo(SimpleMutableList<E> applyTo) {
            applyTo.remove(index);
        }
    }

    private static class MoveChange<E> implements Change<E> {
        private final int from;
        private int to;

        public MoveChange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void applyTo(List<E> applyTo) {
            CollectionUtilsEx.move(applyTo, from, to);
        }

        @Override
        public void applyTo(SimpleMutableList<E> applyTo) {
            applyTo.move(from, to);
        }
    }

    // "case object ClearChange implements Change[Nothing]"
    private static class ClearChange<E> implements Change<E> {
        private static final ClearChange<?> INSTANCE = new ClearChange<>();

        private ClearChange() {
        }

        @SuppressWarnings("unchecked")
        public static <E> ClearChange<E> get() {
            return (ClearChange<E>) INSTANCE;
        }

        @Override
        public void applyTo(List<E> applyTo) {
            applyTo.clear();
        }

        @Override
        public void applyTo(SimpleMutableList<E> applyTo) {
            applyTo.clear();
        }
    }
}
