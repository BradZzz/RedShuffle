package com.bradz.dotdashdot.randomreddit.adapters;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.models.ProfileSub;
import com.bradz.dotdashdot.randomreddit.services.RequestService;
import com.bradz.dotdashdot.randomreddit.utils.Statics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mauve3 on 9/3/16.
 */
public class SubredditAdapter extends BaseAdapter {

  public static final String AUTHORITY = Statics.CONTENTPROVIIDER;
  public String lastClicked = "";
  private static String ERROR = "Parsing Error";

  List<JSONObject> subs;
  LayoutInflater inflater;
  Activity self;
  Account mAccount;
  SharedPreferences sharedpreferences;
  RequestService mRequestService;
  Handler profileHandler;

  public SubredditAdapter(Activity self, List<JSONObject> subs, Account mAccount, SharedPreferences sharedpreferences,
                          RequestService mRequestService, Handler profileHandler) {
    this.self = self;
    inflater = LayoutInflater.from(self);
    this.subs = subs;
    this.mAccount = mAccount;
    this.sharedpreferences = sharedpreferences;
    this.mRequestService = mRequestService;
    this.profileHandler = profileHandler;
  }

  @Override
  public int getCount() {
    return subs.size();
  }

  @Override
  public Object getItem(int i) {
    return subs.get(i);
  }

  @Override
  public long getItemId(int i) {
    return 0;
  }

  public void addItems(List<JSONObject> newSubs) {
    subs.addAll(newSubs);
    Collections.sort(subs, new ProfileSub.SortBasedOnMessageId());
  }

  public void clear() {
    subs.clear();
  }

  public String consumeLastClicked() {
    String temp = lastClicked;
    lastClicked = "";
    return temp;
  }

  public void removeItem(String deletedSub) {
    int index = -1;
    for (int i = 0; i < subs.size(); i++) {
      try {
        if (subs.get(i).getString("display_name").equalsIgnoreCase(deletedSub)) {
          index = i;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    if (index != -1) {
      subs.remove(index);
    }
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {
    View convertView = view;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.row_subreddits, null, true);
    }
    TextView subby = (TextView) convertView.findViewById(R.id.text_subreddit);
    View remove = convertView.findViewById(R.id.remove_subreddit);
    String display_name = "";
    JSONObject jsonObject = subs.get(position);
    try {
      Boolean over_18 = jsonObject.getBoolean("over18");
      display_name = jsonObject.getString("display_name");
      if (over_18) {
        subby.setTextColor(Color.RED);
      } else {
        subby.setTextColor(Color.BLACK);
      }
      subby.setText(display_name);
    } catch (JSONException e) {
      e.printStackTrace();
      subby.setTextColor(Color.RED);
      subby.setText(ERROR);
    }

    final String subName = display_name;

    remove.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(self);
        alertDialog2.setTitle("Confirm Remove");
        alertDialog2.setMessage("Are you sure you want remove this subreddit from your profile?");
        alertDialog2.setIcon(R.drawable.ic_delete);
        alertDialog2.setPositiveButton("Confirm",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              if (lastClicked.equals("")) {
                Toast.makeText(self.getApplicationContext(), "Removing Favorite...", Toast.LENGTH_SHORT).show();
                final Map<String, String> params = new HashMap<>();
                params.put("action", "unsub");
                params.put("sr_name", subName);
                final Map<String, String> headers = new HashMap<>();
                String access_token = sharedpreferences.getString(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN, "");
                if (!access_token.equals("")) {
                  headers.put("Authorization", "bearer " + access_token);
                  Log.i("Favorites", "Sub url: " + Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_SUBREDDITSUBSCRIBE);
                  lastClicked = subName;
                  mRequestService.createPostRequest(self, profileHandler,
                    Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_SUBREDDITSUBSCRIBE, params, headers, Statics.REDDIT_SUBREDDIT_UNSUBSCRIBE);
                } else {
                  Toast.makeText(self.getBaseContext(), "Please login to use the subscribe feature", Toast.LENGTH_SHORT).show();
                }
              } else {
                Toast.makeText(self.getBaseContext(), "Please wait. Unsubscribing from: " + lastClicked, Toast.LENGTH_SHORT).show();
              }
            }
          });
        alertDialog2.setNegativeButton("Cancel",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.cancel();
            }
          });
        alertDialog2.show();
      }
    });

    convertView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, false);
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putString("subreddit", "https://www.reddit.com/r/" + subName + "/about.json");
        //"https://www.reddit.com/r/random/about.json";
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
        self.finish();
      }
    });

    return convertView;
  }
}
