package com.bradz.dotdashdot.randomreddit.helpers.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;

/**
 * Created by Mauve3 on 8/24/16.
 */
public class TABLE_SUB {
    public static void drop(SQLiteDatabase db) {
        db.execSQL("delete from "+ StockDBHelper.TABLE_SUBS);
        db.close();
    }

    public static void delete(SQLiteDatabase db, String selection) {
        db.execSQL("delete from " + StockDBHelper.TABLE_SUBS + " WHERE " + selection);
        db.close();
    }

    public static long add(SQLiteDatabase db, ContentValues values) {

        String sub = values.getAsString(StockDBHelper.COLUMN_SUB);
        Cursor cursor = get(db, "sub = '" + sub + "'", null);
        long rowId = 0;
        if (cursor.getCount() > 0) {
            //cursor.moveToFirst();
            //cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_ID));
        } else {
            rowId = db.replace(StockDBHelper.TABLE_SUBS, null, values);
        }
        cursor.close();
        db.close();
        return rowId;

    }

    public static int updateById(SQLiteDatabase db,String id, ContentValues values) {
        int numRowsChanged = db.update(StockDBHelper.TABLE_SUBS, values, StockDBHelper.COLUMN_ID + " = ?", new String[]{id});
        db.close();
        return numRowsChanged;
    }

    public static int updateBySymbol(SQLiteDatabase db, ContentValues values, String selection) {
        int numRowsChanged = db.update(StockDBHelper.TABLE_SUBS, values, selection, null);
        db.close();
        return numRowsChanged;
    }

    public static boolean has(SQLiteDatabase db, String selection){
        Cursor cursor = db.query(StockDBHelper.TABLE_SUBS, StockDBHelper.ALL_COLUMNS_SUBS, selection, null, null, null, null);
        return cursor.getCount() > 0;
    }

    public static Cursor get(SQLiteDatabase db, String selection, String sortOrder){
        Cursor cursor = db.query(StockDBHelper.TABLE_SUBS, StockDBHelper.ALL_COLUMNS_SUBS, selection, null, null, null, sortOrder);
        return cursor;
    }

    public static Cursor getById(SQLiteDatabase db, String id){
        Cursor cursor = db.query(StockDBHelper.TABLE_SUBS, StockDBHelper.ALL_COLUMNS_SUBS, StockDBHelper.COLUMN_ID+" = ?", new String[]{id}, null, null, null);
        return cursor;
    }
}
