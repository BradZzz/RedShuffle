package com.bradz.dotdashdot.randomreddit.routes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.helpers.tables.TABLE_SUB;
import com.bradz.dotdashdot.randomreddit.helpers.tables.TABLE_THREAD;

/**
 * Created by drewmahrt on 3/6/16.
 */
public class StockPriceContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.bradz.dotdashdot.randomreddit.StockPriceContentProvider";
    private static final String TABLE_THREADS = StockDBHelper.TABLE_THREADS;
    private static final String TABLE_SUBS = StockDBHelper.TABLE_SUBS;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_THREADS);
    public static final Uri CONTENT_URI_SUBS = Uri.parse("content://" + AUTHORITY + "/" + TABLE_SUBS);
    public static final Uri CONTENT_DROP = Uri.parse("content://" + AUTHORITY + "/flush");

    public static final int THREAD = 1;
    public static final int THREAD_ID = 2;
    public static final int FLUSH = 3;

    public static final int SUB = 4;
    //public static final int STOCK_PORTFOLIO = 3;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, TABLE_THREADS, THREAD);
        sURIMatcher.addURI(AUTHORITY, TABLE_THREADS + "/#", THREAD_ID);
        sURIMatcher.addURI(AUTHORITY, "flush", FLUSH);

        sURIMatcher.addURI(AUTHORITY, TABLE_SUBS, SUB);
        //sURIMatcher.addURI(AUTHORITY, STOCK_PRICES_TABLE + "/portfolio", STOCK_PORTFOLIO);
    }

    private StockDBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new StockDBHelper(getContext(), null, null, 1);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriType = sURIMatcher.match(uri);

        Cursor cursor;

        switch (uriType) {
            case THREAD_ID:
                cursor = TABLE_THREAD.getById(dbHelper.getReadable(),uri.getLastPathSegment());
                break;
            case THREAD:
                cursor = TABLE_THREAD.get(dbHelper.getReadable(),selection,sortOrder);
                break;
            case SUB:
                cursor = TABLE_SUB.get(dbHelper.getReadable(),selection,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        long id = 0;
        switch (uriType) {
            case THREAD:
                id = TABLE_THREAD.add(dbHelper.getWritable(),values);
                break;
            case SUB:
                id = TABLE_SUB.add(dbHelper.getWritable(),values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(TABLE_THREADS + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case THREAD:
                TABLE_THREAD.drop(dbHelper.getWritable());
                break;
            case SUB:
                if (!selection.isEmpty()) {
                    TABLE_SUB.delete(dbHelper.getWritable(),selection);
                } else {
                    TABLE_SUB.drop(dbHelper.getWritable());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsUpdated = 0;

        switch (uriType) {
            case THREAD_ID:
                rowsUpdated = TABLE_THREAD.updateById(dbHelper.getWritable(),uri.getLastPathSegment(), values);
                break;
            case THREAD:
                rowsUpdated = TABLE_THREAD.updateBySymbol(dbHelper.getWritable(),values, selection);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    public void drop(){
        Log.i("Dropping!","Dropping!");
        TABLE_THREAD.drop(dbHelper.getWritable());
    }
}
