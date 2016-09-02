package com.bradz.dotdashdot.randomreddit.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.application.ParentApplication;
import com.bradz.dotdashdot.randomreddit.helpers.LoginHelper;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.services.RequestService;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends NavigationActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final Handler profileHandler = new Handler() {

        public void handleMessage(Message msg) {

            Log.i("ProfileActivity","Message handled here!");

            int type = msg.getData().getInt("type");
            long request_time = msg.getData().getLong("request_time");
            String response = msg.getData().getString("response");
            Boolean error = msg.getData().getBoolean("error");

            Log.i("responseHandler","Type: " + type);
            Log.i("responseHandler","Response: " + response);
            Log.i("responseHandler","Error: " + error);

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

                    TextView profileId = (TextView) findViewById(R.id.profile_id);
                    profileId.setText(id);

                    //TextView profileName = (TextView) findViewById(R.id.profile_name);
                    //profileName.setText(name);

                    setActionBar(name);

                    TextView profileCreated = (TextView) findViewById(R.id.profile_created);
                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date(created * 1000));
                    profileCreated.setText("Online Since: " + currentDateTimeString);

                    TextView profileLink = (TextView) findViewById(R.id.profile_link);
                    profileLink.setText(""+link_karma);

                    TextView profileComment = (TextView) findViewById(R.id.profile_comment);
                    profileComment.setText(""+comment_karma);

                    TextView profileEight = (TextView) findViewById(R.id.profile_over_18);
                    profileEight.setText(over_18 ? "Over 18" : "Under 18");

                    TextView profileInbox = (TextView) findViewById(R.id.profile_inbox);
                    profileInbox.setText(""+inbox_count);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mApp = ((ParentApplication) getApplicationContext());
        sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);

        initToolbar();

        Log.i("ProfileActivity","That worked!");
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("ServiceConnection","Service Disconnected");
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("ServiceConnection","Service Connected");
            RequestService.MyBinder myBinder = (RequestService.MyBinder) service;
            mRequestService = myBinder.getService();
            mServiceBound = true;

            initProfile();
        }
    };

    private void initProfile(){

        Log.d("map values","Sharing mapped preferences");

        Map<String,?> keys = sharedpreferences.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());
        }

        if (mServiceBound) {
            final Map<String,String> headers = new HashMap<>();
            String access_token = sharedpreferences.getString(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN,"");
            if (!access_token.equals("")) {
                headers.put("Authorization", "bearer " + access_token);
                mRequestService.createGetRequest(this, profileHandler,
                        Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_PROFILEURL, new HashMap<String, String>(), headers, Statics.REDDIT_PROFILE);
            } else {
                Toast.makeText(getBaseContext(), "Please login to use this feature", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setActionBar(String title){
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

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("onStart","init");
        if (!mServiceBound) {
            Intent intent = new Intent(this, RequestService.class);
            startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        LoginHelper.checkLogin(sharedpreferences, navView);
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
