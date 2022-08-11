package org.nbfalcon.wseminar.androidchessclock.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.BuiltinTimeControls;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.TimeControlStageTemplate;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;
import org.nbfalcon.wseminar.androidchessclock.util.db.TableBackedList;

public class StorageDBHelper extends SQLiteOpenHelper {
    public static final String TABLE_TIME_CONTROLS = "timeControls";
    public static final String[] COLUMNS_TIME_CONTROLS = new String[]{"name", "player1TimeMS", "player1IncrementMS", "player1IncrementMode", "player2TimeMS", "player2IncrementMS", "player2IncrementMode"};
    private static final String DB_NAME = "chessclock";
    private static final int DB_VERSION = 1;


    public StorageDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static SimpleMutableList<ClockPairTemplate> getTimeControlsTableList(SQLiteDatabase db) {
        return new TableBackedList<ClockPairTemplate>(db, TABLE_TIME_CONTROLS, COLUMNS_TIME_CONTROLS) {
            @Override
            protected ClockPairTemplate bindFromCursor(Cursor cursor, int startColumn) {
                String name = cursor.getString(startColumn);

                long p1TimeMS = cursor.getLong(startColumn + 1), p1IncrMS = cursor.getLong(startColumn + 2);
                int p1ModeMS = cursor.getInt(startColumn + 3);
                SingleStageTimeControlTemplate p1 = new SingleStageTimeControlTemplate("FIXME meow", p1TimeMS, p1IncrMS, TimeControlStageTemplate.Type.values()[p1ModeMS]);

                SingleStageTimeControlTemplate p2 = null;
                if (!cursor.isNull(startColumn + 6)) {
                    long p2TimeMS = cursor.getLong(startColumn + 4), p2IncrMS = cursor.getLong(startColumn + 5);
                    int p2ModeMS = cursor.getInt(startColumn + 6);

                    p2 = new SingleStageTimeControlTemplate("FIXME meow", p2TimeMS, p2IncrMS,
                            TimeControlStageTemplate.Type.values()[p2ModeMS]);
                }

                return new ClockPairTemplate(name, p1, p2);
            }

            @Override
            protected void bindToDB(ClockPairTemplate item, ContentValues bindTo) {
                clockPairTemplate2DB(item, bindTo);
            }
        };
    }

    private static void clockPairTemplate2DB(ClockPairTemplate item, ContentValues bindTo) {
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
        db.execSQL("CREATE TABLE " + TABLE_TIME_CONTROLS + " (id INTEGER PRIMARY KEY, name TEXT NOT NULL," + "player1TimeMS INTEGER NOT NULL, player1IncrementMS INTEGER NOT NULL, player1IncrementMode NOT NULL," + "player2TimeMS INTEGER, player2IncrementMS INTEGER, player2IncrementMode INTEGER);");
        initTables(db);
    }

    private void initTables(SQLiteDatabase db) {
        for (ClockPairTemplate template : BuiltinTimeControls.BUILTIN) {
            ContentValues fields = new ContentValues();
            clockPairTemplate2DB(template, fields);
            db.insert(TABLE_TIME_CONTROLS, null, fields);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
