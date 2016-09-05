package com.greyogproducts.greyog.fts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by greyog on 1/04/16.
 */

public class DB {


    private static final String DEF_TABLE = "myTable";

    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    public static String getDefTable() {
        return DEF_TABLE;
    }

    public SQLiteDatabase getDB() {
        return mDB;
    }

    // открываем подключение
    public void open() {
        mDBHelper = DBHelper.getInstance(mCtx);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрываем подключение
    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
//        mCtx.deleteDatabase(DB_NAME);
//        Log.d("Tag","Database deleted: "+DB_NAME);
    }

    // данные по компаниям
    public Cursor getGroupData() {
        String[] columns = new String[]{Constants.ATTR_GROUP_NAME,
                Constants.ATTR_GROUP_ID,
                Constants.ATTR_PRICE,
                Constants.ATTR_TAB_NUM,
                Constants.ATTR_ADVICE_1min,
                Constants.ATTR_ADVICE_5min,
                Constants.ATTR_ADVICE_15min,
                Constants.ATTR_ADVICE_30min,
                Constants.ATTR_ADVICE_1Hour,
                Constants.ATTR_ADVICE_5Hour,
                Constants.ATTR_ADVICE_Day,
                Constants.ATTR_ADVICE_Week
        };
        if (mDB == null) {
            return null;
        }
//        if (!mDB.isOpen()) return null;
        return mDB.query(DEF_TABLE, columns, null, null, null, null, null);
    }

    // данные по телефонам конкретной группы
    public Cursor getChildData(String groupID) {
        String[] columns = new String[]{Constants.ATTR_ADVICE_1min_MABuy,
                Constants.ATTR_ADVICE_5min_MABuy,
                Constants.ATTR_ADVICE_15min_MABuy,
                Constants.ATTR_ADVICE_30min_MABuy,
                Constants.ATTR_ADVICE_1Hour_MABuy,
                Constants.ATTR_ADVICE_5Hour_MABuy,
                Constants.ATTR_ADVICE_Day_MABuy,
                Constants.ATTR_ADVICE_Week_MABuy,
                Constants.ATTR_ADVICE_1min_MASell,
                Constants.ATTR_ADVICE_5min_MASell,
                Constants.ATTR_ADVICE_15min_MASell,
                Constants.ATTR_ADVICE_30min_MASell,
                Constants.ATTR_ADVICE_1Hour_MASell,
                Constants.ATTR_ADVICE_5Hour_MASell,
                Constants.ATTR_ADVICE_Day_MASell,
                Constants.ATTR_ADVICE_Week_MASell,
                Constants.ATTR_ADVICE_1min_IndBuy,
                Constants.ATTR_ADVICE_5min_IndBuy,
                Constants.ATTR_ADVICE_15min_IndBuy,
                Constants.ATTR_ADVICE_30min_IndBuy,
                Constants.ATTR_ADVICE_1Hour_IndBuy,
                Constants.ATTR_ADVICE_5Hour_IndBuy,
                Constants.ATTR_ADVICE_Day_IndBuy,
                Constants.ATTR_ADVICE_Week_IndBuy,
                Constants.ATTR_ADVICE_1min_IndSell,
                Constants.ATTR_ADVICE_5min_IndSell,
                Constants.ATTR_ADVICE_15min_IndSell,
                Constants.ATTR_ADVICE_30min_IndSell,
                Constants.ATTR_ADVICE_1Hour_IndSell,
                Constants.ATTR_ADVICE_5Hour_IndSell,
                Constants.ATTR_ADVICE_Day_IndSell,
                Constants.ATTR_ADVICE_Week_IndSell
        };
        if (mDB == null) {
            return null;
        }
        return mDB.query(DEF_TABLE, columns, Constants.ATTR_GROUP_ID + " = \'"
                + groupID + "\'", null, null, null, null);
    }
}

class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "myDB";
    private static final int DB_VERSION = 1;
    private static final String DEF_TABLE = "myTable";

    private static DBHelper sInstance;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создаем таблицу с полями
//            Log.d("Tag","Deleting table...");
//            db.execSQL("drop table if exists " + DEF_TABLE);
        Log.d("Tag", "Creating table...");
        db.execSQL("create table " + DEF_TABLE + " ("
                + "id integer primary key autoincrement,"
                + Constants.ATTR_GROUP_NAME + " text default '',"
                + Constants.ATTR_GROUP_ID + " text default '',"
                + Constants.ATTR_PRICE + " text default '',"
                + Constants.ATTR_TAB_NUM + " text default '0',"
                + Constants.ATTR_ADVICE_1min + " text default '',"
                + Constants.ATTR_ADVICE_5min + " text default '',"
                + Constants.ATTR_ADVICE_15min + " text default '',"
                + Constants.ATTR_ADVICE_30min + " text default '',"
                + Constants.ATTR_ADVICE_1Hour + " text default '',"
                + Constants.ATTR_ADVICE_5Hour + " text default '',"
                + Constants.ATTR_ADVICE_Day + " text default '',"
                + Constants.ATTR_ADVICE_Week + " text default '',"
                + Constants.ATTR_ADVICE_1min_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_5min_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_15min_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_30min_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_1Hour_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_5Hour_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_Day_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_Week_MABuy + " text default '',"
                + Constants.ATTR_ADVICE_1min_MASell + " text default '',"
                + Constants.ATTR_ADVICE_5min_MASell + " text default '',"
                + Constants.ATTR_ADVICE_15min_MASell + " text default '',"
                + Constants.ATTR_ADVICE_30min_MASell + " text default '',"
                + Constants.ATTR_ADVICE_1Hour_MASell + " text default '',"
                + Constants.ATTR_ADVICE_5Hour_MASell + " text default '',"
                + Constants.ATTR_ADVICE_Day_MASell + " text default '',"
                + Constants.ATTR_ADVICE_Week_MASell + " text default '',"
                + Constants.ATTR_ADVICE_1min_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_5min_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_15min_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_30min_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_1Hour_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_5Hour_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_Day_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_Week_IndBuy + " text default '',"
                + Constants.ATTR_ADVICE_1min_IndSell + " text default '',"
                + Constants.ATTR_ADVICE_5min_IndSell + " text default '',"
                + Constants.ATTR_ADVICE_15min_IndSell + " text default '',"
                + Constants.ATTR_ADVICE_30min_IndSell + " text default '',"
                + Constants.ATTR_ADVICE_1Hour_IndSell + " text default '',"
                + Constants.ATTR_ADVICE_5Hour_IndSell + " text default '',"
                + Constants.ATTR_ADVICE_Day_IndSell + " text default '',"
                + Constants.ATTR_ADVICE_Week_IndSell + " text default ''"
                + ");");
        Log.d("Tag", "Created table.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
