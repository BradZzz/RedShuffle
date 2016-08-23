package com.bradz.dotdashdot.randomreddit.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StockDBHelper extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "redditDB.db";

  public static final String TABLE_THREADS = "THREADS";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_TITLE = "title";
  public static final String COLUMN_URL = "url";
  public static final String COLUMN_VOTES = "votes";
  public static final String COLUMN_SUB = "sub";
  public static final String COLUMN_IMAGE = "image";

  public static final String[] ALL_COLUMNS = new String[]{COLUMN_ID,COLUMN_TITLE,COLUMN_URL,COLUMN_VOTES,COLUMN_SUB,COLUMN_IMAGE};

  public StockDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, DATABASE_NAME, factory, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
            TABLE_THREADS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_URL + " TEXT,"
            + COLUMN_VOTES + " INTEGER,"
            + COLUMN_SUB + " TEXT,"
            + COLUMN_IMAGE + " TEXT)";
    db.execSQL(CREATE_PRODUCTS_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_THREADS);
    onCreate(db);
  }

  public void dropThreads() {
    SQLiteDatabase db = getWritableDatabase();
    db.execSQL("delete from "+ TABLE_THREADS);
  }

  public long addThread(ContentValues values) {

    SQLiteDatabase db = getWritableDatabase();
    long rowId = db.replace(TABLE_THREADS, null, values);
    db.close();

    return rowId;
  }

  public int updateThreadById(String id, ContentValues values) {
    SQLiteDatabase db = getWritableDatabase();

    int numRowsChanged = db.update(TABLE_THREADS, values, COLUMN_ID + " = ?", new String[]{id});
    db.close();

    return numRowsChanged;
  }

  public int updateThreadBySymbol(ContentValues values, String selection) {
    SQLiteDatabase db = getWritableDatabase();

    int numRowsChanged = db.update(TABLE_THREADS, values, selection, null);
    db.close();

    return numRowsChanged;
  }

  public Cursor getThreads(String selection, String sortOrder){
    SQLiteDatabase db = getReadableDatabase();

    Cursor cursor = db.query(TABLE_THREADS, ALL_COLUMNS, selection, null, null, null, sortOrder);

    return cursor;
  }

  public Cursor getThreadById(String id){
    SQLiteDatabase db = getReadableDatabase();

    Cursor cursor = db.query(TABLE_THREADS, ALL_COLUMNS, COLUMN_ID+" = ?", new String[]{id}, null, null, null);

    return cursor;
  }
}