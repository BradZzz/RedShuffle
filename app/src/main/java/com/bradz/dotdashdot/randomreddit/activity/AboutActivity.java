package com.bradz.dotdashdot.randomreddit.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.application.ParentApplication;
import com.bradz.dotdashdot.randomreddit.helpers.LoginHelper;
import com.bradz.dotdashdot.randomreddit.services.RequestService;
import com.bradz.dotdashdot.randomreddit.utils.Statics;

public class AboutActivity extends NavigationActivity {
  private String TAG = this.getClass().getCanonicalName();
  Activity self;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    self = this;

    ParentApplication application = (ParentApplication) getApplication();
    mTracker = application.getDefaultTracker();

    mApp = ((ParentApplication) getApplicationContext());
    sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);

    initToolbar();
  }

  private ServiceConnection mServiceConnection = new ServiceConnection() {

    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.i("ServiceConnection", "Service Disconnected");
      mServiceBound = false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      Log.i("ServiceConnection", "Service Connected");
      RequestService.MyBinder myBinder = (RequestService.MyBinder) service;
      mRequestService = myBinder.getService();
      mServiceBound = true;

      if (!LoginHelper.checkLogin(sharedpreferences, navView)) {
        refreshLogin();
      }
    }
  };

  @Override
  protected void onStart() {
    super.onStart();
    Log.i("onStart", "init");
    if (!mServiceBound) {
      Intent intent = new Intent(this, RequestService.class);
      startService(intent);
      bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    writeScreenAnalytics(TAG, "onResume");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mServiceBound) {
      unbindService(mServiceConnection);
      mServiceBound = false;
    }
  }
}
