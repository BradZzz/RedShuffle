package com.bradz.dotdashdot.randomreddit.activity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.application.ParentApplication;
import com.bradz.dotdashdot.randomreddit.helpers.LoginHelper;
import com.bradz.dotdashdot.randomreddit.services.RequestService;
import com.bradz.dotdashdot.randomreddit.utils.Statics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mauve3 on 9/1/16.
 */
public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public SharedPreferences sharedpreferences;
    public NavigationView navView;
    public RequestService mRequestService;
    public boolean mServiceBound = false;
    public ParentApplication mApp;

    public final Handler responseHandler = new Handler() {

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
            if (Statics.REDDIT_LOGIN1 == type && !error) {
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    String access_token = jsonObject.getString("access_token");
                    int expires_in = jsonObject.getInt("expires_in");
                    String refresh_token = jsonObject.getString("refresh_token");
                    long expire_millis = request_time + (expires_in * 1000);

                    if (sharedpreferences != null){
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN, access_token);
                        editor.putLong(Statics.SHAREDSETTINGS_REDDITEXPIRES, expire_millis);
                        editor.putString(Statics.SHAREDSETTINGS_REDDITREFRESHTOKEN, refresh_token);
                        editor.apply();

                        Toast.makeText(getBaseContext(), "Logged into account", Toast.LENGTH_SHORT).show();
                        LoginHelper.setLogIn(navView);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public ServiceConnection mServiceConnection = new ServiceConnection() {

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
        }
    };

    public void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        //Reddit login request
        if (requestCode == Statics.REDDIT_LOGIN1) {
            String url = data.getStringExtra("url");
            Uri uri=Uri.parse(url);
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");

            System.out.println("Code: " + code);
            System.out.println("State: " + state);

            final Map<String,String> params = new HashMap<>();
            params.put("grant_type", "authorization_code");
            params.put("code", code);
            params.put("redirect_uri", Statics.REDDIT_REDIRECT_URL);

            final Map<String,String> headers = new HashMap<>();
            try {
                headers.put("Authorization", "Basic " + Base64.encodeToString((Statics.REDDIT_CLIENT_ID + ":").getBytes("UTF-8"), Base64.DEFAULT));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (mServiceBound) {
                Log.i("onActivityResult","Contacting bound service");
                mRequestService.createPostRequest(this, responseHandler,
                        "https://www.reddit.com/api/v1/access_token", params, headers, requestCode);
            } else {
                Log.i("onActivityResult","No service bound");
            }
        }

        //Google donate purchase
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                JSONObject jo = null;
                try {
                    jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Toast.makeText(getApplicationContext(), "Thanks for your purchase of: " + sku, Toast.LENGTH_SHORT).show();
                }
                catch (JSONException e) {
                    //alert("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            //finish();
        } else if (id == R.id.nav_profile) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            //finish();
        } else if (id == R.id.nav_favorites) {
            Intent i = new Intent(this, FavoriteActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            Toast.makeText( getBaseContext(), "Logged out", Toast.LENGTH_SHORT).show();
            LoginHelper.setLogOut(sharedpreferences, navView);
        } else if (id == R.id.nav_login) {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(i, Statics.REDDIT_LOGIN1);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_donate) {
            try {
                Bundle buyIntentBundle = mApp.getBillingConnection().getBuyIntent(3, getPackageName(),
                        Statics.PURCHASES_DONATION_DOLLAR, "inapp", Statics.DEVELOPER_CALLBACK_ID);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                if (pendingIntent != null) {
                    startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                }
            } catch (RemoteException | IntentSender.SendIntentException e) {
                Toast.makeText(getApplicationContext(), "Something went fucking wrong", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
