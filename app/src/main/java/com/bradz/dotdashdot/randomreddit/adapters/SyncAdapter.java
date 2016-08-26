package com.bradz.dotdashdot.randomreddit.adapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.models.Stock;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.utils.Statics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drewmahrt on 3/7/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    ContentResolver mResolver;

    private static final String AUTHORITY = Statics.CONTENTPROVIIDER;
    private static final String STOCKS_TABLE = "stocks";
    private static RequestQueue queue;

    public static final Uri SYMBOLS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + STOCKS_TABLE + "/symbols");

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mResolver = context.getContentResolver();
        if (queue == null) {
            queue = Volley.newRequestQueue(getContext());
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(SyncAdapter.class.getName(),"Starting sync");
        //mResolver.query(StockPriceContentProvider.CONTENT_DROP,null,null,null,null);
        //mResolver.delete(StockPriceContentProvider.CONTENT_URI,null,null);

        //getPortfolioStocks();
        getRandomBadSubs();

    }

    /*private void getPortfolioStocks(){
        Cursor cursor = mResolver.query(SYMBOLS_CONTENT_URI,null,"exchange = 'NYSE'",null,null);
        while(cursor != null && cursor.moveToNext()) {
            updateStockInfo(cursor.getString(0),true);
        }
    }*/

    private void getRandomBadSubs(){
        String stockUrl = "https://www.reddit.com/r/random.json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                stockUrl, (JSONObject) null, // here
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String sub = "";
                            List<Stock> threads = new ArrayList<Stock>();

                            JSONObject data = response.getJSONObject("data");
                            JSONArray children = data.getJSONArray("children");
                            for (int i = 0; i < children.length(); i++) {
                                JSONObject child = children.getJSONObject(i);
                                JSONObject data2 = child.getJSONObject("data");
                                if (sub.equals("")) {

                                    sub = data2.getString("subreddit");
                                }
                                Stock thread = new Stock(
                                        data2.getString("title"),
                                        data2.getString("url"),
                                        data2.getString("thumbnail"),
                                        Integer.valueOf(data2.getString("ups"))

                                );
                                threads.add(thread);
                            }
                            updateStockInfo(threads, sub);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        queue.add(request);
    }

    public void updateStockInfo(List<Stock> threads, String sub){

        //Cursor checkCursor = mResolver.query(StockPriceContentProvider.CONTENT_URI,null,null,null,null);
        /*if (checkCursor.moveToFirst()){
            for (int i = 0; i < checkCursor.getCount(); i++) {
                checkCursor.moveToPosition(i);
                if (threads.size() > i) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(StockDBHelper.COLUMN_TITLE, threads.get(i).getTitle());
                    contentValues.put(StockDBHelper.COLUMN_URL, threads.get(i).getUrl());
                    contentValues.put(StockDBHelper.COLUMN_VOTES, threads.get(i).getVotes());
                    contentValues.put(StockDBHelper.COLUMN_SUB, sub);
                    contentValues.put(StockDBHelper.COLUMN_IMAGE, threads.get(i).getImage());
                    mResolver.update(StockPriceContentProvider.CONTENT_URI, contentValues, "_id='" + checkCursor.getColumnIndex("_id") + "'", null);
                }
            }
        } else {*/
        mResolver.delete(StockPriceContentProvider.CONTENT_URI,null,null);
        for (int i = 0; i < threads.size(); i++) {
            //checkCursor.moveToPosition(i);
            Log.i("Inserting", threads.get(i).getTitle() + ":" + threads.get(i).getUrl() + ":"
                    + threads.get(i).getVotes() + ":" + sub + ":"
                    + threads.get(i).getImage());
            ContentValues contentValues = new ContentValues();
            contentValues.put(StockDBHelper.COLUMN_TITLE, threads.get(i).getTitle());
            contentValues.put(StockDBHelper.COLUMN_URL, threads.get(i).getUrl());
            contentValues.put(StockDBHelper.COLUMN_VOTES, threads.get(i).getVotes());
            contentValues.put(StockDBHelper.COLUMN_SUB, sub);
            contentValues.put(StockDBHelper.COLUMN_IMAGE, threads.get(i).getImage());
            mResolver.insert(StockPriceContentProvider.CONTENT_URI, contentValues);
        }
        //}
        //checkCursor.close();
    }
}


