package com.bradz.dotdashdot.randomreddit.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.android.volley.RequestQueue;
import com.bradz.dotdashdot.randomreddit.adapters.SyncAdapter;
import com.bradz.dotdashdot.randomreddit.helpers.FakeAuthenticator;
import com.bradz.dotdashdot.randomreddit.helpers.HttpRequest;

import java.util.Map;

/**
 * Created by drewmahrt on 3/7/16.
 */
public class RequestService extends Service {

    private IBinder mBinder = new MyBinder();

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    private static HttpRequest httpRequest;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
            if (httpRequest == null) {
                httpRequest = new HttpRequest();
            }
        }
    }

    public void createGetRequest(Context context, final Handler handler, final String url,
                                  final Map<String,String> params, final Map<String,String> headers, final int type){
        httpRequest.createGetRequest(context, handler, url, params, headers, type);
    }

    public void createPostRequest(Context context, final Handler handler, final String url,
                                  final Map<String,String> params, final Map<String,String> headers, final int type){
        httpRequest.createPostRequest(context, handler, url, params, headers, type);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public RequestService getService() {
            return RequestService.this;
        }
    }
}
