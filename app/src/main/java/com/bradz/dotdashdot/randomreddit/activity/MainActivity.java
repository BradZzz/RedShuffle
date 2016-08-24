package com.bradz.dotdashdot.randomreddit.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.koushikdutta.ion.Ion;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CursorAdapter mCursorAdapter;

    private TextView mUpdatedTextView;
    private String TAG = this.getClass().getCanonicalName();

    //private static final int CONTENTPROVIDER = R.string.CONTENTPROVIDER;
    //private static Statics statics;

    SharedPreferences sharedpreferences;
    public static final String AUTHORITY = Statics.CONTENTPROVIIDER;

    // Account type
    public static final String ACCOUNT_TYPE = "example.com";
    // Account
    public static final String ACCOUNT = "default_account";

    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initWidgets();

        //statics = new Statics(this, new int[]{CONTENTPROVIDER});

        sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);
        mAccount = createSyncAccount(this);

        getContentResolver().registerContentObserver(StockPriceContentProvider.CONTENT_URI,true,new StockContentObserver(new Handler()));

        //getContentResolver().query(StockPriceContentProvider.CONTENT_DROP,null,null,null,null);
        Cursor existingStocksCursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI,null,null,null,null);

        //cursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,existingStocksCursor,new String[]{StockDBHelper.COLUMN_STOCK_SYMBOL,StockDBHelper.COLUMN_STOCK_PRICE},new int[]{android.R.id.text1,android.R.id.text2},0);
        mCursorAdapter = new CursorAdapter(this,existingStocksCursor,0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.article_row_layout,parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView subreddit = (TextView) findViewById(R.id.subreddit);

                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);

                ImageView sub_image = (ImageView) view.findViewById(R.id.sub_image);
                sub_image.setClipToOutline(true);

                view.setBackgroundResource(android.R.color.background_light);

                String title = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_TITLE));
                final String url = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_URL));
                String image = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_IMAGE));
                String sub = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_SUB));
                int votes = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_VOTES));

                Log.i("Subreddit","Sub: " + sub);

                if (!subreddit.getText().equals(sub)) {
                    String subby = "/" + sub;
                    subreddit.setText(subby);
                }

                text1.setText(title);


                String voteString = "Votes: " + votes;
                text2.setText(voteString);

                Ion.with(sub_image)
                    .placeholder(R.drawable.reddit_logo)
                    .error(R.drawable.reddit_logo)
                    //.animateLoad(spinAnimation)
                    //.animateIn(fadeInAnimation)
                    .load(image);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        String linkUrl = url;
                        if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
                            linkUrl = "http://" + linkUrl;
                        }
                        i.setData(Uri.parse(linkUrl));
                        startActivity(i);
                    }
                });
            }
        };


        ListView listView = (ListView) findViewById(R.id.stock_price_list);
        listView.setAdapter(mCursorAdapter);

        mUpdatedTextView = (TextView) findViewById(R.id.updated_text);

        /*Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
        ContentResolver.setSyncAutomatically(mAccount,AUTHORITY,true);
        ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, 90);*/

        decideSync();
    }

    private void decideSync(){
        Boolean checked = sharedpreferences.getBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE,true);
        Log.i(TAG,"Checking: " + checked);
        if (checked){
            startSync();
        } else {
            stopSync();
        }
    }

    private void startSync(){
        Log.i(TAG,"startSync");
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
        ContentResolver.setSyncAutomatically(mAccount,AUTHORITY,true);
        ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, 90);
    }

    private void stopSync(){
        Log.i(TAG,"stopSync");

        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 0);
        ContentResolver.cancelSync(mAccount, AUTHORITY);
    }

    private void initWidgets(){
        SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.switch_compat);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.i(TAG,"Checked Change");
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE,isChecked);
                editor.apply();
                decideSync();
            }
        });
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will eventually save your sub!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public class StockContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public StockContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //do stuff on UI thread
            Log.d(MainActivity.class.getName(),"Changed observed at "+uri);

            //getContentResolver().query(StockPriceContentProvider.CONTENT_DROP,null,null,null,null);
            mCursorAdapter.swapCursor(getContentResolver().query(StockPriceContentProvider.CONTENT_URI, null, null, null, null));

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            mUpdatedTextView.setText("Last updated: "+currentDateTimeString);
        }
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_favorites) {

        } else if (id == R.id.nav_logout) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
