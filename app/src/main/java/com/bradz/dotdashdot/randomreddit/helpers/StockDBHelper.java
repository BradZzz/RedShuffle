package com.bradz.dotdashdot.randomreddit.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StockDBHelper extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 4;
  private static final String DATABASE_NAME = "redditDB.db";

  //Columns that are included in multiple tables
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_SUB = "sub";
  public static final String COLUMN_IMAGE = "image";
  public static final String COLUMN_NSFW = "nsfw";
  public static final String COLUMN_CREATED = "created";

  //Just the subs table
  public static final String TABLE_SUBS = "SUBS";
  public static final String COLUMN_DESCRIPTION = "description";
  public static final String COLUMN_FAVORITE = "favorite";
  public static final String COLUMN_USERS = "users";
  public static final String COLUMN_SEEN = "seen";

  //Just the threads table
  public static final String TABLE_THREADS = "THREADS";
  public static final String COLUMN_TITLE = "title";
  public static final String COLUMN_URL = "url";
  public static final String COLUMN_VOTES = "votes";
  public static final String COLUMN_FULL_IMAGE = "full_image";

  public static final String[] ALL_COLUMNS_THREAD = new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_URL,
          COLUMN_VOTES, COLUMN_SUB, COLUMN_IMAGE, COLUMN_FULL_IMAGE, COLUMN_NSFW, COLUMN_CREATED};
  public static final String[] ALL_COLUMNS_SUBS = new String[]{COLUMN_ID, COLUMN_IMAGE, COLUMN_SUB,
          COLUMN_FAVORITE, COLUMN_SEEN, COLUMN_DESCRIPTION, COLUMN_NSFW, COLUMN_USERS, COLUMN_CREATED};

  public StockDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, DATABASE_NAME, factory, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String CREATE_THREADS_TABLE = "CREATE TABLE " +
            TABLE_THREADS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_URL + " TEXT,"
            + COLUMN_VOTES + " INTEGER,"
            + COLUMN_SUB + " TEXT,"
            + COLUMN_FULL_IMAGE + " TEXT,"
            + COLUMN_IMAGE + " TEXT,"
            + COLUMN_NSFW + " INTEGER,"
            + COLUMN_CREATED + " TEXT)";
    db.execSQL(CREATE_THREADS_TABLE);

    String CREATE_SUBS_TABLE = "CREATE TABLE " +
            TABLE_SUBS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_SUB + " TEXT,"
            + COLUMN_IMAGE + " TEXT,"
            + COLUMN_SEEN + " INTEGER,"
            + COLUMN_FAVORITE + " INTEGER,"
            + COLUMN_USERS + " INTEGER,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_NSFW + " INTEGER,"
            + COLUMN_CREATED + " TEXT)";
    db.execSQL(CREATE_SUBS_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_THREADS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBS);
    onCreate(db);
  }

  public SQLiteDatabase getWritable(){
    return getWritableDatabase();
  }

  public SQLiteDatabase getReadable(){
    return getReadableDatabase();
  }

}