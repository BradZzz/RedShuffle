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
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.ListView;
import android.widget.TextView;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.koushikdutta.ion.Ion;

import java.text.DateFormat;
import java.util.Date;

public class FavoriteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CursorAdapter mCursorAdapter;
    ContentResolver mResolver;

    private TextView mUpdatedTextView;
    private String TAG = this.getClass().getCanonicalName();

    SharedPreferences sharedpreferences;
    public static final String AUTHORITY = Statics.CONTENTPROVIIDER;

    // Account type
    public static final String ACCOUNT_TYPE = "example.com";
    // Account
    public static final String ACCOUNT = "default_account";

    //private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initToolbar();
        //initWidgets();

        //statics = new Statics(this, new int[]{CONTENTPROVIDER});

        mResolver = getContentResolver();
        sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);
        //mAccount = createSyncAccount(this);

        getContentResolver().registerContentObserver(StockPriceContentProvider.CONTENT_URI,true,new StockContentObserver(new Handler()));

        //getContentResolver().query(StockPriceContentProvider.CONTENT_DROP,null,null,null,null);
        Cursor existingStocksCursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI_SUBS,null,null,null,null);

        //cursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,existingStocksCursor,new String[]{StockDBHelper.COLUMN_STOCK_SYMBOL,StockDBHelper.COLUMN_STOCK_PRICE},new int[]{android.R.id.text1,android.R.id.text2},0);
        mCursorAdapter = new CursorAdapter(this,existingStocksCursor,0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.favorites_row_layout,parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView sub = (TextView) view.findViewById(R.id.sub);
                TextView seen = (TextView) view.findViewById(R.id.seen);

                view.setBackgroundResource(android.R.color.background_light);

                String subreddit = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_SUB));
                int seenSub = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_SEEN));

                sub.setText(subreddit);
                String seenS = ""+seenSub;
                seen.setText(seenS);
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

        //decideSync();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will eventually save your sub!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                TextView subreddit = (TextView) findViewById(R.id.subreddit);

                ContentValues contentValues = new ContentValues();
                contentValues.put(StockDBHelper.COLUMN_SUB, subreddit.getText().toString());
                contentValues.put(StockDBHelper.COLUMN_FAVORITE, 1);
                contentValues.put(StockDBHelper.COLUMN_SEEN, 1);
                mResolver.insert(StockPriceContentProvider.CONTENT_URI_SUBS, contentValues);
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
            Log.d(FavoriteActivity.class.getName(),"Changed observed at "+uri);

            //getContentResolver().query(StockPriceContentProvider.CONTENT_DROP,null,null,null,null);
            mCursorAdapter.swapCursor(getContentResolver().query(StockPriceContentProvider.CONTENT_URI, null, null, null, null));

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            mUpdatedTextView.setText("Last updated: "+currentDateTimeString);
        }
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
