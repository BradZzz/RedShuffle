package com.bradz.dotdashdot.randomreddit.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.koushikdutta.ion.Ion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FavoriteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CursorAdapter mCursorAdapter;
    ContentResolver mResolver;
    private String TAG = this.getClass().getCanonicalName();
    SharedPreferences sharedpreferences;
    public static final String AUTHORITY = Statics.CONTENTPROVIIDER;
    private Account mAccount;
    // Account type
    public static final String ACCOUNT_TYPE = "example.com";
    // Account
    public static final String ACCOUNT = "default_account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mAccount = createSyncAccount(this);

        initToolbar();
        mResolver = getContentResolver();
        sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);
        //Cursor existingStocksCursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI_SUBS,null,null,null,null);
        mCursorAdapter = new CursorAdapter(this,getFavorites(),0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.favorites_row_layout,parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final TextView sub = (TextView) view.findViewById(R.id.sub);
                TextView titleText = (TextView) view.findViewById(R.id.title);
                //TextView nsfwText = (TextView) view.findViewById(R.id.nsfw);
                TextView usersText = (TextView) view.findViewById(R.id.users);

                ImageView sub_image = (ImageView) view.findViewById(R.id.sub_image);
                sub_image.setClipToOutline(true);

                view.setBackgroundResource(android.R.color.background_light);

                final String subreddit = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_SUB));
                String title = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_DESCRIPTION));
                //int users = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_USERS));
                int nsfw = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_NSFW));
                //int nsfw = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_NSFW));
                String image = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_IMAGE));
                String created = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_CREATED));
                //int nsfw = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_NSFW));

                if (nsfw > 0) {
                    LinearLayout outline = (LinearLayout) view.findViewById(R.id.image_outline);
                    outline.setBackground(getResources().getDrawable(R.drawable.red_border));
                }

                sub.setText(subreddit);
                /*if (nsfw > 0) {
                    nsfwText.setVisibility(View.VISIBLE);
                    nsfwText.setTextColor(Color.RED);
                    nsfwText.setText("nsfw");
                } else {
                    nsfwText.setVisibility(View.GONE);
                }*/

                /*String usersString = "Users: "+users;
                usersText.setText(usersString);*/

                SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy HH:mm:ss");//dd/MM/yyyy
                Date now = new Date(Long.valueOf(created));
                String strDate = sdfDate.format(now);

                //Calendar calendar = Calendar.getInstance();
                //calendar.setTimeInMillis(Long.valueOf(created));

                //String createdString = calendar.getTime().toString();
                usersText.setText(strDate);

                titleText.setText(title);
                //String seenS = "Seen: "+seenSub;
                //seen.setText(seenS);

                Ion.with(sub_image)
                        .placeholder(R.drawable.reddit_logo)
                        .error(R.drawable.reddit_logo)
                        //.animateLoad(spinAnimation)
                        //.animateIn(fadeInAnimation)
                        .load(image);

                View remove = view.findViewById(R.id.remove_fav);
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(FavoriteActivity.this);
                        alertDialog2.setTitle("Confirm Remove");
                        alertDialog2.setMessage("Are you sure you want remove this favorite?");
                        alertDialog2.setIcon(R.drawable.ic_delete);
                        alertDialog2.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    getContentResolver().delete(StockPriceContentProvider.CONTENT_URI_SUBS,"sub = '" + subreddit + "'",null);
                                    Toast.makeText(getApplicationContext(), "Removed Favorite", Toast.LENGTH_SHORT).show();
                                    mCursorAdapter.swapCursor(getFavorites());
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

                View subscribe = view.findViewById(R.id.subscribe_sub);
                subscribe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(), "This subscribes to the subreddit: " + subreddit, Toast.LENGTH_SHORT).show();
                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
                        ContentResolver.setSyncAutomatically(mAccount,AUTHORITY,false);

                        Bundle settingsBundle = new Bundle();
                        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                        settingsBundle.putString("subreddit", "https://www.reddit.com/r/" + subreddit + "/about.json");
                        //"https://www.reddit.com/r/random/about.json";
                        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
                        finish();
                    }
                });
            }
        };

        ListView listView = (ListView) findViewById(R.id.stock_price_list);
        listView.setAdapter(mCursorAdapter);
    }

    private Cursor getFavorites(){
        return getContentResolver().query(StockPriceContentProvider.CONTENT_URI_SUBS,null,null,null,null);
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            finish();
        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_favorites) {
            /*Intent i = new Intent(this, FavoriteActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);*/
        } else if (id == R.id.nav_logout) {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(i, 1337);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_donate) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        Log.i("createSyncAccount","Creating Account");

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
          /*
           * If you don't set android:syncable="true" in
           * in your <provider> element in the manifest,
           * then call context.setIsSyncable(account, AUTHORITY, 1)
           * here.
           */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }
}
