package com.bradz.dotdashdot.randomreddit.activity;

import android.accounts.Account;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.adapters.SubredditAdapter;
import com.bradz.dotdashdot.randomreddit.application.ParentApplication;
import com.bradz.dotdashdot.randomreddit.helpers.LoginHelper;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.models.ProfileSub;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.services.RequestService;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends NavigationActivity {
  private String TAG = this.getClass().getCanonicalName();

  public final Handler profileHandler = new Handler() {

    public void handleMessage(Message msg) {

      Log.i("ProfileActivity", "Message handled here!");

      int type = msg.getData().getInt("type");
      //long request_time = msg.getData().getLong("request_time");
      String response = msg.getData().getString("response");
      Boolean error = msg.getData().getBoolean("error");

      Log.i("responseHandler", "Type: " + type);
      Log.i("responseHandler", "Response: " + response);
      Log.i("responseHandler", "Error: " + error);

      //If the user has logged into the app, save the token into shared preferences
      if (Statics.REDDIT_PROFILE == type && !error) {
        try {
          JSONObject jsonObject = new JSONObject(response);
          String id = jsonObject.getString("id");
          String name = jsonObject.getString("name");
          Long created = jsonObject.getLong("created");
          Long link_karma = jsonObject.getLong("link_karma");
          Long comment_karma = jsonObject.getLong("comment_karma");
          boolean over_18 = jsonObject.getBoolean("over_18");
          int inbox_count = jsonObject.getInt("inbox_count");

                    /*TextView profileId = (TextView) findViewById(R.id.profile_id);
                    profileId.setText("ID: " + id);*/

          setActionBar(name);

                    /*TextView profileCreated = (TextView) findViewById(R.id.profile_created);
                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date(created * 1000));
                    profileCreated.setText("Since: " + currentDateTimeString);*/

          TextView profileLink = (TextView) findViewById(R.id.profile_link);
          profileLink.setText("" + link_karma);

          TextView profileComment = (TextView) findViewById(R.id.profile_comment);
          profileComment.setText("" + comment_karma);

                    /*TextView profileEight = (TextView) findViewById(R.id.profile_over_18);
                    profileEight.setText(over_18 ? "Over 18" : "Under 18");*/

                    /*TextView profileInbox = (TextView) findViewById(R.id.profile_inbox);
                    profileInbox.setText(""+inbox_count);*/

        } catch (JSONException e) {
          e.printStackTrace();
        }
      } else if (Statics.REDDIT_PROFILE == type && error) {
        Toast.makeText(getBaseContext(), "Error showing user profile", Toast.LENGTH_SHORT).show();
      }

      //If the user has logged into the app, save the token into shared preferences
      if (Statics.REDDIT_SUBREDDIT_UNSUBSCRIBE == type) {
        if (!error) {
          load_icon.setVisibility(View.VISIBLE);
          writeEventAnalytics("Sub", "Profile - Remove");
          removeFavorite(adapty.consumeLastClicked());
          adapty.clear();
          listy.invalidate();
          final Map<String, String> headers = new HashMap<>();
          String access_token = sharedpreferences.getString(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN, "");
          if (!access_token.equals("")) {
            headers.put("Authorization", "bearer " + access_token);
            callSubreddit(new HashMap<String, String>(), headers);
          }
          Toast.makeText(getApplicationContext(), "Successfully unsubscribed from subreddit!", Toast.LENGTH_SHORT).show();
        } else {
          adapty.consumeLastClicked();
          Toast.makeText(getApplicationContext(), "Error unsubscribing to subreddit!", Toast.LENGTH_SHORT).show();
        }
      }

      if (Statics.REDDIT_SUBREDDITS == type && !error) {
        JSONObject jsonObject = null;
        List<JSONObject> jsonlisty = new ArrayList<>();

        String after = "";
        //String before = "";

        try {
          jsonObject = new JSONObject(response);
          JSONObject data = jsonObject.getJSONObject("data");
                    /*if (!data.isNull("before")) {
                        before = data.getString("before");
                        Log.i(TAG,"Before: " + before);
                    }*/
          if (!data.isNull("after")) {
            after = data.getString("after");
            Log.i(TAG, "After: " + after);
            final Map<String, String> params = new HashMap<>();
            params.put("after", after);
            callSubreddit(params);
          } else {
            load_icon.setVisibility(View.GONE);
          }
          JSONArray children = data.getJSONArray("children");
          for (int i = 0; i < children.length(); i++) {
            JSONObject child  = children.getJSONObject(i).getJSONObject("data");
            jsonlisty.add(child);
            try {
              String display_name = child.getString("display_name");
              getFavorite(display_name);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }

        if (listy.getChildCount() == 0) {
          Collections.sort(jsonlisty, new ProfileSub.SortBasedOnMessageId());
          adapty = new SubredditAdapter(self, jsonlisty, mAccount,
            sharedpreferences, mRequestService, profileHandler);
          listy.invalidate();
          listy.setAdapter(adapty);
        } else {
          //add to list and invalidate
          adapty.addItems(jsonlisty);
          listy.invalidate();
          listy.setAdapter(adapty);
        }

      } else if (Statics.REDDIT_SUBREDDITS == type && error) {
        Toast.makeText(getBaseContext(), "Error showing subreddits", Toast.LENGTH_SHORT).show();
      }
    }
  };

  SubredditAdapter adapty;
  ListView listy;
  Activity self;
  private Account mAccount;
  private ScrollView scrolly;
  private AVLoadingIndicatorView load_icon;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    self = this;

    ParentApplication application = (ParentApplication) getApplication();
    mTracker = application.getDefaultTracker();

    mAccount = createSyncAccount(this);
    mApp = ((ParentApplication) getApplicationContext());
    sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);

    listy = (ListView) findViewById(R.id.profile_listy);
    adapty = new SubredditAdapter(this, new ArrayList<JSONObject>(), mAccount,
      sharedpreferences, mRequestService, profileHandler);
    listy.setAdapter(adapty);

    load_icon = (AVLoadingIndicatorView) findViewById(R.id.load_indicator);
    load_icon.setVisibility(View.VISIBLE);

    initToolbar();

    Log.i("ProfileActivity", "That worked!");
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

      initProfile();

      if (!LoginHelper.checkLogin(sharedpreferences, navView)) {
        refreshLogin();
      }
    }
  };

  private void initProfile() {

    Log.d("map values", "Sharing mapped preferences");

    Map<String, ?> keys = sharedpreferences.getAll();

    for (Map.Entry<String, ?> entry : keys.entrySet()) {
      Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
    }

    if (mServiceBound) {
      final Map<String, String> headers = new HashMap<>();
      String access_token = sharedpreferences.getString(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN, "");
      if (!access_token.equals("")) {
        headers.put("Authorization", "bearer " + access_token);
        mRequestService.createGetRequest(this, profileHandler,
          Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_PROFILEURL, new HashMap<String, String>(), headers, Statics.REDDIT_PROFILE);

        callSubreddit(new HashMap<String, String>(), headers);

      } else {
        Toast.makeText(getBaseContext(), "Please login to use this feature", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  private void callSubreddit(Map<String, String> params) {
    String sParams = "?";
    for (String param : params.keySet()) {
      sParams += param + "=" + params.get(param) + "&";
    }

    final Map<String, String> headers = new HashMap<>();
    String access_token = sharedpreferences.getString(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN, "");
    if (!access_token.equals("")) {
      headers.put("Authorization", "bearer " + access_token);
      mRequestService.createGetRequest(this, profileHandler,
        Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_SUBREDDITSURL + sParams, params, headers, Statics.REDDIT_SUBREDDITS);
    } else {
      Toast.makeText(getBaseContext(), "Error Connecting to profile. Please login again", Toast.LENGTH_SHORT).show();
    }
  }

  private void callSubreddit(Map<String, String> params, Map<String, String> headers) {
    mRequestService.createGetRequest(this, profileHandler,
      Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_SUBREDDITSURL, params, headers, Statics.REDDIT_SUBREDDITS);
  }

  private void setActionBar(String title) {
    if (getActionBar() != null) {
      getActionBar().setDisplayShowTitleEnabled(false);
      getActionBar().setDisplayShowCustomEnabled(true);

      LayoutInflater inflator = LayoutInflater.from(this);
      View titleToolbar = inflator.inflate(R.layout.titleview, null);
      TextView tView = ((TextView) titleToolbar.findViewById(R.id.title));
      tView.setText(title);
      getActionBar().setCustomView(titleToolbar);
    }
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayShowCustomEnabled(true);

      LayoutInflater inflator = LayoutInflater.from(this);
      View titleToolbar = inflator.inflate(R.layout.titleview, null);
      TextView tView = ((TextView) titleToolbar.findViewById(R.id.title));
      tView.setText(title);
      getSupportActionBar().setCustomView(titleToolbar);
    }
  }

  private void getFavorite(String name) {
    Cursor c = getContentResolver().query(StockPriceContentProvider.CONTENT_URI_SUBS, null, StockDBHelper.COLUMN_SUB + " = '" + name + "'", null, null);

    for (int i = 0; i < c.getCount(); i++) {
      c.moveToPosition(i);
      final String subreddit = c.getString(c.getColumnIndex(StockDBHelper.COLUMN_SUB));
      Log.i("getFavorite", "sub = " + subreddit);

      int id = c.getInt(c.getColumnIndex(StockDBHelper.COLUMN_ID));
      ContentValues contentValues = new ContentValues();
      contentValues.put(StockDBHelper.COLUMN_FAVORITE, 2);

      getContentResolver().update(Uri.parse(StockPriceContentProvider.CONTENT_URI_SUBS_BASE + "/" + id), contentValues, null, null);
    }
  }

  private void removeFavorite(String name) {
    Cursor c = getContentResolver().query(StockPriceContentProvider.CONTENT_URI_SUBS, null, StockDBHelper.COLUMN_SUB + " = '" + name + "'", null, null);

    for (int i = 0; i < c.getCount(); i++) {
      c.moveToPosition(i);
      final String subreddit = c.getString(c.getColumnIndex(StockDBHelper.COLUMN_SUB));
      Log.i("getFavorite", "sub = " + subreddit);

      int id = c.getInt(c.getColumnIndex(StockDBHelper.COLUMN_ID));
      ContentValues contentValues = new ContentValues();
      contentValues.put(StockDBHelper.COLUMN_FAVORITE, 1);

      getContentResolver().update(Uri.parse(StockPriceContentProvider.CONTENT_URI_SUBS_BASE + "/" + id), contentValues, null, null);
    }
  }

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
