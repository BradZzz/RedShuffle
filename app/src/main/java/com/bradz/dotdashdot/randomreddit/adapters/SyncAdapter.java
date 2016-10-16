package com.bradz.dotdashdot.randomreddit.adapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.models.Thread;
import com.bradz.dotdashdot.randomreddit.models.Subreddit;
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

    private SharedPreferences sharedpreferences;
    private static RequestQueue queue;
    private String TAG = getClass().getCanonicalName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mResolver = context.getContentResolver();
        if (queue == null) {
            queue = Volley.newRequestQueue(getContext());
        }
        sharedpreferences = context.getSharedPreferences(Statics.CURRENTSUB, Context.MODE_PRIVATE);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(SyncAdapter.class.getName(),"Starting sync");
        String sub = "";
        String after = "";
        try {
            sub = extras.getString("subreddit","");
            if (!extras.getString("after","").equals("")) {
                after = extras.getString("after","");
            }
        } catch (NullPointerException e ) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (sub.equals("")) {
                sub = "https://www.reddit.com/r/random/about.json";
            }
        }
        Log.i("This Sub","This Sub: " + sub);
        queue.getCache().clear();
        if (after.equals("")) {
            getRandomBadSub(sub);
        } else {
            getRandomBadSubs(new Subreddit(sub,"","","",0,"",false),"?after="+after);
        }

    }

    private void getRandomBadSub(String subUrl){
        //String subUrl = "https://www.reddit.com/r/random/about.json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, subUrl, (JSONObject) null, // here
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                Log.i("onResponse", "Responded");
                try {
                    JSONObject data = response.getJSONObject("data");
                    Subreddit sub = new Subreddit(
                        data.getString("display_name"),
                        data.getString("public_description"),
                        data.getString("description"),
                        data.getString("title"),
                        data.getInt("subscribers"),
                        data.getString("header_title"),
                        Boolean.valueOf(data.getString("over18"))
                    );

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Statics.CURRENTSUB_DISPLAYNAME,sub.getDisplay_name());
                    editor.putString(Statics.CURRENTSUB_DESCRIPTION,sub.getDescription());
                    editor.putString(Statics.CURRENTSUB_PUBLICDESCRIPTION,sub.getPublic_description());
                    editor.putString(Statics.CURRENTSUB_TITLE,sub.getTitle());
                    editor.putInt(Statics.CURRENTSUB_SUBSCRIBERS,sub.getSubscribers());
                    editor.putString(Statics.CURRENTSUB_HEADERTITLE,sub.getHeader_title());
                    editor.putBoolean(Statics.CURRENTSUB_NSFW,sub.isNsfw());
                    editor.apply();

                    getRandomBadSubs(sub,"");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
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

    private void getRandomBadSubs(final Subreddit sub, final String params){

        String threadUrl = "https://www.reddit.com/r/" + sub.getDisplay_name() + ".json" + params;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, threadUrl, (JSONObject) null, // here
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        List<Thread> threads = new ArrayList<Thread>();
                        JSONObject data = response.getJSONObject("data");
                        JSONArray children = data.getJSONArray("children");
                        for (int i = 0; i < children.length(); i++) {
                            JSONObject child = children.getJSONObject(i);
                            JSONObject data2 = child.getJSONObject("data");
                            //This checks for bigger images
                            String image_url = "";
                            if (data2.has("preview") && !data2.isNull("preview")){
                                JSONObject preview = data2.getJSONObject("preview");
                                JSONArray images = preview.getJSONArray("images");
                                if (images.length() > 0) {
                                    JSONObject firstpreview = images.getJSONObject(0);
                                    JSONObject source = firstpreview.getJSONObject("source");
                                    image_url = source.getString("url");
                                }
                            }

                            Thread thread = new Thread(
                                data2.getString("title"),
                                data2.getString("url"),
                                data2.getString("thumbnail"),
                                Integer.valueOf(data2.getString("ups")),
                                image_url,
                                data2.getString("permalink"),
                                Boolean.valueOf(data2.getString("over_18"))
                            );
                            threads.add(thread);
                        }

                        String after = "";
                        if (!data.isNull("after")) {
                            after = data.getString("after");
                        }

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Statics.CURRENTSUB_NEXT, after);
                        editor.apply();

                        updateStockInfo(threads,sub, params.equals(""));
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
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

    public void updateStockInfo(List<Thread> threads, Subreddit sub, boolean deleting){
        if (deleting) {
            mResolver.delete(StockPriceContentProvider.CONTENT_URI, null, null);
        }
        for (int i = 0; i < threads.size(); i++) {
            //checkCursor.moveToPosition(i);
            /*Log.i("Inserting", threads.get(i).getTitle() + ":" + threads.get(i).getUrl() + ":"
                    + threads.get(i).getVotes() + ":" + sub + ":"
                    + threads.get(i).getImage());*/

            ContentValues contentValues = new ContentValues();
            contentValues.put(StockDBHelper.COLUMN_TITLE, threads.get(i).getTitle());
            contentValues.put(StockDBHelper.COLUMN_URL, threads.get(i).getUrl());
            contentValues.put(StockDBHelper.COLUMN_VOTES, threads.get(i).getVotes());
            contentValues.put(StockDBHelper.COLUMN_SUB, sub.getDisplay_name());
            contentValues.put(StockDBHelper.COLUMN_IMAGE, threads.get(i).getImage());
            contentValues.put(StockDBHelper.COLUMN_COMMENTS, threads.get(i).getComments());
            contentValues.put(StockDBHelper.COLUMN_NSFW, threads.get(i).isNsfw());
            contentValues.put(StockDBHelper.COLUMN_FULL_IMAGE, threads.get(i).getFull_image());
            contentValues.put(StockDBHelper.COLUMN_CREATED, "" + (System.currentTimeMillis()));

            mResolver.insert(StockPriceContentProvider.CONTENT_URI, contentValues);
        }
    }
}


