package org.nbfalcon.wseminar.androidchessclock.util.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.nbfalcon.wseminar.androidchessclock.util.CollectionUtilsEx;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TableBackedList<E> implements SimpleMutableList<E> {
    public static final long SORT_ID_GAP = 1000;
    public static final int SORT_ID_BASE = 1;
    private final ArrayList<E> items = new ArrayList<>();
    private final ArrayList<Long> sortIds = new ArrayList<>();

    private final SQLiteDatabase db;
    private final String table;
    private final String[] columns;

    protected TableBackedList(SQLiteDatabase db, String table, String[] columns) {
        this.db = db;
        this.table = table;
        this.columns = columns;
        load();
    }

    @SuppressWarnings("SameParameterValue")
    protected abstract E bindFromCursor(Cursor cursor, int startColumn);

    protected abstract void bindToDB(E item, ContentValues bindTo);

    protected abstract void saveRowId(E item, long rowId);

    protected abstract long getRowId(E item);

    private void load() {
        assert sortIds.isEmpty() && items.isEmpty();

        String selectPart = "sortId" + CollectionUtilsEx.joinPrefix(columns, ", ");
        try (Cursor rowIter = db.rawQuery("SELECT " + selectPart + " FROM " + table + " ORDER BY sortId", new String[]{})) {
            while (rowIter.moveToNext()) {
                sortIds.add(rowIter.getLong(0));
                items.add(bindFromCursor(rowIter, 1));
            }
        }
    }

    @Override
    public void add(E item) {
        long lastId = !sortIds.isEmpty() ? sortIds.get(sortIds.size() - 1) : SORT_ID_BASE;
        long newId = lastId + SORT_ID_GAP;
        sortIds.add(newId);

        ContentValues fields = new ContentValues();
        bindToDB(item, fields);
        fields.put("sortId", newId);

        long rowId = db.insert(table, null, fields);
        saveRowId(item, rowId);

        items.add(item);
        sortIds.add(newId);
    }

    @Override
    public void add(int index, E item) {
        db.beginTransaction();

        long mySortId = acquireSortIdAt(index);
        sortIds.add(index, mySortId);

        ContentValues fields = new ContentValues();
        bindToDB(item, fields);
        fields.put("sortId", sortIds.get(index));
        long rowId = db.insert(table, null, fields);
        saveRowId(item, rowId);

        db.setTransactionSuccessful();
        db.endTransaction();

        items.add(index, item);
        List<Long> laterSortIDs = sortIds.subList(index + 1, sortIds.size());
        for (int i = 0; i < laterSortIDs.size(); i++) {
            laterSortIDs.set(i, laterSortIDs.get(i) + 10);
        }
    }

    private long acquireSortIdAt(int index) {
        long mySortId;
        if (sortIds.isEmpty()) {
            mySortId = SORT_ID_BASE;
        } else if (index == 0) {
            mySortId = sortIds.get(0) - SORT_ID_GAP;
        } else if (index == sortIds.size()) {
            mySortId = sortIds.get(sortIds.size() - 1) + SORT_ID_GAP;
        } else if (sortIds.get(index - 1) < sortIds.get(index) - 1) {
            // We have a gap
            mySortId = (sortIds.get(index - 1) + sortIds.get(index)) / 2;
        } else {
            mySortId = sortIds.get(index);
            db.execSQL("UPDATE timeControls SET sortId = sortId + " + SORT_ID_GAP + " WHERE sortId >= ?", new Object[]{mySortId});
        }
        return mySortId;
    }

    @Override
    public void set(int index, E newValue) {
        ContentValues fields = new ContentValues();
        bindToDB(newValue, fields);
        // Kinda hacky to not use rowId here, but it doesn't matter; sortId is also UNIQUE
        db.update(table, fields, "sortId = ?", new String[]{sortIds.get(index).toString()});

        items.set(index, newValue);
    }

    @Override
    public E get(int index) {
        return items.get(index);
    }

    @Override
    public void remove(int index) {
        db.delete(table, "sortId = ?", new String[]{sortIds.get(index).toString()});
        items.remove(index);
        sortIds.remove(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public void clear() {
        db.execSQL("DELETE FROM " + table);
        items.clear();
        sortIds.clear();
    }

    @Override
    public void move(int from, int to) {
        if (to == from + 1 || to == from - 1) {
            long aid = getRowId(items.get(from)), bid = getRowId(items.get(to));

            // Just swap rowids
            db.beginTransaction();
            db.execSQL("UPDATE " + table + " SET rowid = (case WHEN rowid = ? THEN -? ELSE -? END) WHERE rowid IN (?, ?)",
                    new Object[]{aid, bid, aid, aid, bid});
            db.execSQL("UPDATE " + table + " SET rowid = -rowid WHERE rowid < 0");
            db.setTransactionSuccessful();
            db.endTransaction();

            Collections.swap(items, from, to);
        }
        else {
            long myNewSortId = acquireSortIdAt(to);
            sortIds.set(from, myNewSortId);
            CollectionUtilsEx.move(sortIds, from, to);
            CollectionUtilsEx.move(items, from, to);
        }
    }

    @Override
    public E[] toArray(E[] a) {
        return items.toArray(a);
    }
}
