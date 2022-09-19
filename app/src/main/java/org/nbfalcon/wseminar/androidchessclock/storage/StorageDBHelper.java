package org.nbfalcon.wseminar.androidchessclock.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.BuiltinTimeControls;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;
import org.nbfalcon.wseminar.androidchessclock.util.db.TableBackedList;

public class StorageDBHelper extends SQLiteOpenHelper {
    public static final String TABLE_TIME_CONTROLS = "timeControls";
    public static final String[] COLUMNS_TIME_CONTROLS = new String[]{"id", "name", "player1TimeMS", "player1IncrementMS", "player1IncrementMode", "player2TimeMS", "player2IncrementMS", "player2IncrementMode"};
    private static final String DB_NAME = "chessclock";
    private static final int DB_VERSION = 1;


    public StorageDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static SimpleMutableList<ClockPairTemplate> getTimeControlsTableList(SQLiteDatabase db) {
        return new TableBackedList<ClockPairTemplate>(db, TABLE_TIME_CONTROLS, COLUMNS_TIME_CONTROLS) {
            @Override
            protected ClockPairTemplate bindFromCursor(Cursor cursor, int startColumn) {
                return clockPairTemplateFromDB(cursor, startColumn);
            }

            @Override
            protected void bindToDB(ClockPairTemplate item, ContentValues bindTo) {
                clockPairTemplate2DB(item, bindTo);
            }

            @Override
            protected void saveRowId(ClockPairTemplate item, long rowId) {
                item.dbRowId = rowId;
            }

            @Override
            protected long getRowId(ClockPairTemplate item) {
                return item.dbRowId;
            }

            @Override
            public long getRowId(int pos) {
                return get(pos).dbRowId;
            }
        };
    }

    @NotNull
    private static ClockPairTemplate clockPairTemplateFromDB(Cursor cursor, int startColumn) {
        long id = cursor.getLong(startColumn);

        String name = cursor.getString(startColumn + 1);

        long p1TimeMS = cursor.getLong(startColumn + 2), p1IncrMS = cursor.getLong(startColumn + 3);
        int p1ModeMS = cursor.getInt(startColumn + 4);
        SingleStageTimeControlTemplate p1 = new SingleStageTimeControlTemplate(p1TimeMS, p1IncrMS, TimeControlStageTemplate.Type.values()[p1ModeMS]);

        SingleStageTimeControlTemplate p2 = null;
        if (!cursor.isNull(startColumn + 7)) {
            long p2TimeMS = cursor.getLong(startColumn + 5), p2IncrMS = cursor.getLong(startColumn + 6);
            int p2ModeMS = cursor.getInt(startColumn + 7);

            p2 = new SingleStageTimeControlTemplate(p2TimeMS, p2IncrMS, TimeControlStageTemplate.Type.values()[p2ModeMS]);
        }

        ClockPairTemplate result = new ClockPairTemplate(name, p1, p2);
        result.dbRowId = id;
        return result;
    }

    private static void clockPairTemplate2DB(ClockPairTemplate item, ContentValues bindTo) {
        // Don't save the id: it'll be derived from context
        bindTo.put("name", item.toString());

        SingleStageTimeControlTemplate p1 = (SingleStageTimeControlTemplate) item.getPlayer1();
        bindTo.put("player1TimeMS", p1.time);
        bindTo.put("player1IncrementMS", p1.increment);
        bindTo.put("player1IncrementMode", p1.type.ordinal());

        if (!item.setForBothPlayers()) {
            SingleStageTimeControlTemplate p2 = (SingleStageTimeControlTemplate) item.getPlayer2();
            bindTo.put("player2TimeMS", p2.time);
            bindTo.put("player2IncrementMS", p2.increment);
            bindTo.put("player2IncrementMode", p2.type.ordinal());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TIME_CONTROLS
                + " (sortId INTEGER UNIQUE NOT NULL, id INTEGER PRIMARY KEY, name TEXT NOT NULL,"
                + "player1TimeMS INTEGER NOT NULL, player1IncrementMS INTEGER NOT NULL, player1IncrementMode NOT NULL,"
                + "player2TimeMS INTEGER, player2IncrementMS INTEGER, player2IncrementMode INTEGER);");
        initTables(db);
    }

    private void initTables(SQLiteDatabase db) {
        for (int i = 0; i < BuiltinTimeControls.BUILTIN.length; i++) {
            ContentValues fields = new ContentValues();
            clockPairTemplate2DB(BuiltinTimeControls.BUILTIN[i], fields);
            fields.put("sortId", TableBackedList.SORT_ID_GAP * i);
            long rowId = db.insert(TABLE_TIME_CONTROLS, null, fields);
            assert rowId != -1;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
