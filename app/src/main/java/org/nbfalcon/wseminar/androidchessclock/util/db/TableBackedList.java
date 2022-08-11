package org.nbfalcon.wseminar.androidchessclock.util.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.nbfalcon.wseminar.androidchessclock.util.CollectionUtilsEx;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

import java.util.ArrayList;
import java.util.List;

public abstract class TableBackedList<E> implements SimpleMutableList<E> {
    public static final int ID_GAP = 10;
    private final ArrayList<E> items = new ArrayList<>();
    // FIXME: make this a long
    private final ArrayList<Integer> ids = new ArrayList<>();

    private final SQLiteDatabase db;
    private final String table;
    private final String[] columns;

    protected TableBackedList(SQLiteDatabase db, String table, String[] columns) {
        this.db = db;
        this.table = table;
        this.columns = columns;
        load(); // FIXME: async
    }

    @SuppressWarnings("SameParameterValue")
    protected abstract E bindFromCursor(Cursor cursor, int startColumn);

    protected abstract void bindToDB(E item, ContentValues bindTo);

    private void load() {
        assert ids.isEmpty() && items.isEmpty();

        // FIXME: columns.length == 0?
        String selectPart = "id, " + CollectionUtilsEx.join(columns, ", ");
        try (Cursor rowIter = db.rawQuery("SELECT " + selectPart + " FROM " + table, new String[]{})) {
            while (rowIter.moveToNext()) {
                ids.add(rowIter.getInt(0));
                items.add(bindFromCursor(rowIter, 1));
            }
        }
    }

    @Override
    public void add(E item) {
        int lastId = !ids.isEmpty() ? ids.get(ids.size() - 1) : 1;
        int newId = lastId + ID_GAP;
        ids.add(newId);

        ContentValues fields = new ContentValues();
        bindToDB(item, fields);
        fields.put("id", newId);

        db.insert(table, null, fields);

        items.add(item);
        ids.add(newId);
    }

    @Override
    public void add(int index, E item) {
        db.beginTransaction();

        // FIXME: leverage a gap when possible
        db.execSQL("UPDATE timeControls SET id = id + " + ID_GAP + " WHERE id > ?", new Object[]{ids.get(index)});

        ContentValues fields = new ContentValues();
        bindToDB(item, fields);
        fields.put("id", ids.get(index));
        db.insert(table, null, fields);

        db.setTransactionSuccessful();
        db.endTransaction();

        items.add(index, item);
        ids.add(index, ids.get(index));
        List<Integer> laterRowIDs = ids.subList(index + 1, ids.size());
        for (int i = 0; i < laterRowIDs.size(); i++) {
            laterRowIDs.set(i, laterRowIDs.get(i) + 10);
        }
    }

    @Override
    public void set(int index, E newValue) {
        ContentValues fields = new ContentValues();
        bindToDB(newValue, fields);
        db.update(table, fields, "id = ?", new String[]{ids.get(index).toString()});

        items.set(index, newValue);
    }

    @Override
    public E get(int index) {
        return items.get(index);
    }

    @Override
    public void remove(int index) {
        db.delete(table, "id = ?", new String[]{ids.get(index).toString()});
        items.remove(index);
        ids.remove(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public void clear() {
        db.execSQL("DELETE FROM " + table);
    }

    @Override
    public void move(int from, int to) {
        SimpleMutableList.super.move(from, to);
    }
}
