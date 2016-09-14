package com.bradz.dotdashdot.randomreddit.helpers.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;

/**
 * Created by Mauve3 on 8/24/16.
 */
public class TABLE_THREAD {
    public static void drop(SQLiteDatabase db) {
        db.execSQL("delete from "+ StockDBHelper.TABLE_THREADS);
        db.close();
    }

    public static long add(SQLiteDatabase db, ContentValues values) {
        String url = values.getAsString(StockDBHelper.COLUMN_URL);
        Cursor cursor = get(db, StockDBHelper.COLUMN_URL + " = '" + url + "'", null);

        long rowId = 0;
        if (cursor.getCount() > 0) {
            //cursor.moveToFirst();
            //cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_ID));
            //Log.i("add","adding url: " + url);
        } else {
            rowId = db.replace(StockDBHelper.TABLE_THREADS, null, values);
        }
        cursor.close();
        db.close();
        return rowId;
    }

    public static int updateById(SQLiteDatabase db,String id, ContentValues values) {
        int numRowsChanged = db.update(StockDBHelper.TABLE_THREADS, values, StockDBHelper.COLUMN_ID + " = ?", new String[]{id});
        db.close();
        return numRowsChanged;
    }

    public static int updateBySymbol(SQLiteDatabase db, ContentValues values, String selection) {
        int numRowsChanged = db.update(StockDBHelper.TABLE_THREADS, values, selection, null);
        db.close();
        return numRowsChanged;
    }

    public static Cursor get(SQLiteDatabase db, String selection, String sortOrder){
        Cursor cursor = db.query(StockDBHelper.TABLE_THREADS, StockDBHelper.ALL_COLUMNS_THREAD, selection, null, null, null, sortOrder);
        return cursor;
    }

    public static Cursor getById(SQLiteDatabase db, String id){
        Cursor cursor = db.query(StockDBHelper.TABLE_THREADS, StockDBHelper.ALL_COLUMNS_THREAD, StockDBHelper.COLUMN_ID+" = ?", new String[]{id}, null, null, null);
        return cursor;
    }
}
