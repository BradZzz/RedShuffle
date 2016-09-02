package com.bradz.dotdashdot.randomreddit.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bradz.dotdashdot.randomreddit.models.Subreddit;
import com.bradz.dotdashdot.randomreddit.utils.Statics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mauve3 on 8/31/16.
 */
public class HttpRequest {

    private static RequestQueue queue;

    public void createGetRequest(Context context, final Handler handler, final String url,
                                 final Map<String,String> params, final Map<String,String> headers, final int type){

        final long expires = System.currentTimeMillis();

        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (JSONObject) null, // here
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("onResponse", "Responded");
                        Message msg = Message.obtain();
                        Bundle b = new Bundle();
                        b.putInt("type", type);
                        b.putLong("request_time", expires);
                        b.putString("response", String.valueOf(response));
                        b.putBoolean("error", false);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message msg = Message.obtain();
                        Bundle b = new Bundle();
                        b.putInt("type", type);
                        b.putLong("request_time", expires);
                        b.putString("response", error.getMessage());
                        b.putBoolean("error", true);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        headers.put("Content-Type","application/json");
                        return headers;
                    }
        };
        queue.add(request);
    }

    public void createPostRequest(Context context, final Handler handler, final String url,
                                  final Map<String,String> params, final Map<String,String> headers, final int type){

        final long expires = System.currentTimeMillis();

        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message msg = Message.obtain();
                Bundle b = new Bundle();
                b.putInt("type", type);
                b.putLong("request_time", expires);
                b.putString("response", response);
                b.putBoolean("error", false);
                msg.setData(b);
                handler.sendMessage(msg);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = Message.obtain();
                Bundle b = new Bundle();
                b.putInt("type", type);
                b.putLong("request_time", expires);
                b.putString("response", error.getMessage());
                b.putBoolean("error", true);
                msg.setData(b);
                handler.sendMessage(msg);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        queue.add(post);
    }
}
