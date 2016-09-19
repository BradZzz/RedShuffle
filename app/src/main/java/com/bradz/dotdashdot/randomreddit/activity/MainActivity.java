package com.bradz.dotdashdot.randomreddit.activity;

import android.accounts.Account;
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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.application.ParentApplication;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.services.RequestService;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends NavigationActivity {
    private String TAG = this.getClass().getCanonicalName();
    private CursorAdapter mCursorAdapter;
    private ContentResolver mResolver;
    private Context self;
    private TextView mUpdatedTextView;
    private AVLoadingIndicatorView load_icon;

    //private static final int CONTENTPROVIDER = R.string.CONTENTPROVIDER;
    //private static Statics statics;

    //SharedPreferences sharedpreferences,
    SharedPreferences subredditpreferences;
    public static final String AUTHORITY = Statics.CONTENTPROVIIDER;

    // Account type
    public static final String ACCOUNT_TYPE = "example.com";
    // Account
    public static final String ACCOUNT = "default_account";

    private String first_image = null;

    private Account mAccount;

    private View titleToolbar;
    private TextView tView;
    private ListView listView;
    private View toggleDescription;
    //NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        self = this;

        ParentApplication application = (ParentApplication) getApplication();
        mTracker = application.getDefaultTracker();

        sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);
        subredditpreferences = getSharedPreferences(Statics.CURRENTSUB, Context.MODE_PRIVATE);

        initToolbar();
        //initButtons();
        initWidgets();
        //initRequestService();
        initActionBar();

        mApp = ((ParentApplication) getApplicationContext());

        mResolver = getContentResolver();
        mAccount = createSyncAccount(this);

        getContentResolver().registerContentObserver(StockPriceContentProvider.CONTENT_URI,true,new StockContentObserver(new Handler()));

        Cursor existingStocksCursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI,null,null,null,null);

        tView = ((TextView) titleToolbar.findViewById(R.id.title));

        mCursorAdapter = new CursorAdapter(this,existingStocksCursor,0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.article_row_layout,parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                //TextView subreddit = (TextView) findViewById(R.id.subreddit);

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
                int nsfw = cursor.getInt(cursor.getColumnIndex(StockDBHelper.COLUMN_NSFW));

                LinearLayout outline = (LinearLayout) view.findViewById(R.id.image_outline);
                if (nsfw > 0) {
                    outline.setBackground(getResources().getDrawable(R.drawable.red_border));
                } else {
                    outline.setBackground(getResources().getDrawable(R.drawable.black_border));
                }

                if (first_image == null && image != null && !image.isEmpty() && image.contains("http")) {
                    first_image = image;
                }
                String subby = "/" + sub;
                if (tView != null && !tView.getText().equals(subby)) {
                    tView.setText(subby);
                    writeEventAnalytics("Sub","New",sub);
                }
                text1.setText(title);
                String voteString = "Votes: " + votes;
                text2.setText(voteString);

                Ion.with(sub_image)
                    .placeholder(R.drawable.reddit_logo)
                    .error(R.drawable.reddit_logo)
                    .load(image);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        String linkUrl = url.replace("amp;","");
                        if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
                            linkUrl = "http://" + linkUrl;
                        }
                        i.setData(Uri.parse(linkUrl));
                        startActivity(i);
                    }
                });

                //setDescription();
            }
        };


        listView = (ListView) findViewById(R.id.stock_price_list);
        listView.setAdapter(mCursorAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener(){

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                //Algorithm to check if the last item is visible or not
                final int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount){

                    load_icon.setVisibility(View.VISIBLE);

                    Bundle settingsBundle = new Bundle();
                    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                    String sub = subredditpreferences.getString(Statics.CURRENTSUB_DISPLAYNAME,"");
                    String after = subredditpreferences.getString(Statics.CURRENTSUB_NEXT,"");

                    //String sub = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_SUB));

                    if (!sub.equals("")) {
                        settingsBundle.putString("subreddit", sub);
                        if (!after.equals("")) {
                            settingsBundle.putString("after", after);
                        }

                        // you have reached end of list, load more data
                        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
                        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, false);
                        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
                    }
                }
            }
        });
        /*listView.setOnBottomReachedListener(new ScrollListView.OnBottomReachedListener() {
            @Override
            public void onBottomReached() {

            }
        });*/

        setDate(true);
        decideSync();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        writeScreenAnalytics(TAG,"onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    /*private void initRequestService(){
        Intent intent = new Intent(this, RequestService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }*/

    private void initActionBar(){
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowCustomEnabled(true);

            LayoutInflater inflator = LayoutInflater.from(this);
            titleToolbar = inflator.inflate(R.layout.titleview, null);
            TextView tView = ((TextView) titleToolbar.findViewById(R.id.title));
            tView.setText("something");
            getActionBar().setCustomView(titleToolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);

            LayoutInflater inflator = LayoutInflater.from(this);
            titleToolbar = inflator.inflate(R.layout.titleview, null);
            TextView tView = ((TextView) titleToolbar.findViewById(R.id.title));
            tView.setText("something else");
            getSupportActionBar().setCustomView(titleToolbar);
        }
    }

    private void decideSync(){
        Boolean checked = sharedpreferences.getBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE,false);
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
        ContentResolver.setSyncAutomatically(mAccount,AUTHORITY,true);
        ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, 60);
    }

    private void stopSync(){
        Log.i(TAG,"stopSync");
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 0);
        ContentResolver.cancelSync(mAccount, AUTHORITY);
    }

    private void initWidgets(){
        load_icon = (AVLoadingIndicatorView) findViewById(R.id.load_indicator);
        load_icon.setVisibility(View.GONE);

        SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.switch_compat);
        switchCompat.setChecked(sharedpreferences.getBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE, false));
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.i(TAG,"Checked Change");
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE,isChecked);
                editor.apply();

                Toast.makeText(getApplicationContext(), "Auto Update Enabled: " + isChecked, Toast.LENGTH_SHORT).show();

                decideSync();
            }
        });
    }

    @Override
    public void initToolbar(){
        navView = (NavigationView) findViewById(R.id.nav_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_fav = (FloatingActionButton) findViewById(R.id.fab_fav);
        fab_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //Snackbar.make(view, "Sub added to favorites", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            Toast.makeText(getApplicationContext(), "Sub added to favorites", Toast.LENGTH_SHORT).show();

            if (tView != null && !tView.getText().toString().isEmpty()) {
                ContentValues contentValues = new ContentValues();

                String sub = tView.getText().toString().replace("/", "");

                writeEventAnalytics("Sub","Fav",sub);

                contentValues.put(StockDBHelper.COLUMN_SUB, sub);
                if (first_image != null) {
                    contentValues.put(StockDBHelper.COLUMN_IMAGE, first_image);
                }

                //contentValues.put(StockDBHelper.COLUMN_FAVORITE, 1);
                contentValues.put(StockDBHelper.COLUMN_SEEN, 1);

                String title = subredditpreferences.getString(Statics.CURRENTSUB_TITLE,"");
                contentValues.put(StockDBHelper.COLUMN_DESCRIPTION, title);

                /*String publicDesc = getDescription();
                if (!publicDesc.equals("")) {
                    contentValues.put(StockDBHelper.COLUMN_DESCRIPTION, publicDesc);
                }*/

                Boolean nsfw = subredditpreferences.getBoolean(Statics.CURRENTSUB_NSFW,false);
                contentValues.put(StockDBHelper.COLUMN_NSFW, nsfw);

                int subscribers = subredditpreferences.getInt(Statics.CURRENTSUB_SUBSCRIBERS,0);
                contentValues.put(StockDBHelper.COLUMN_USERS, subscribers);

                contentValues.put(StockDBHelper.COLUMN_CREATED, "" + (System.currentTimeMillis()));
                mResolver.insert(StockPriceContentProvider.CONTENT_URI_SUBS, contentValues);

                Animation animation = AnimationUtils.loadAnimation(self, R.anim.expand_contract);
                view.startAnimation(animation);
            }
            }
        });

        FloatingActionButton fab_reroll = (FloatingActionButton) findViewById(R.id.fab_reroll);
        fab_reroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Rerolling...", Toast.LENGTH_SHORT).show();

                writeEventAnalytics("App","Reroll");

                ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(mAccount,AUTHORITY,false);

                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);

                Animation animation = AnimationUtils.loadAnimation(self, R.anim.rotate_center);
                view.startAnimation(animation);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

            load_icon.setVisibility(View.GONE);

            //do stuff on UI thread
            Log.d(MainActivity.class.getName(),"Change observed at "+uri);

            Cursor cursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI,null,null,null,null);
            Log.d(MainActivity.class.getName(),"Cursor count: "+cursor.getCount());
            if (cursor.getCount() > 27) {
                setDate(false);
            } else {
                setDate(true);
            }
            //getContentResolver().query(StockPriceContentProvider.CONTENT_DROP,null,null,null,null);
            mCursorAdapter.swapCursor(cursor);
        }
    }

    private void setDate(boolean scroll){
        first_image = null;

        mUpdatedTextView = (TextView) findViewById(R.id.updated_text);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        mUpdatedTextView.setText("Last updated: "+currentDateTimeString);
        setDescription(scroll);
    }

    private String getDescription(){
        String publicDesc = subredditpreferences.getString(Statics.CURRENTSUB_PUBLICDESCRIPTION,"");
        String description = subredditpreferences.getString(Statics.CURRENTSUB_DESCRIPTION,"");
        String title = subredditpreferences.getString(Statics.CURRENTSUB_TITLE,"");

        Log.i("getDescription","publicDesc: " + publicDesc);
        Log.i("getDescription","description: " + description);
        Log.i("getDescription","title: " + title);

        if (!publicDesc.equals("")) {
            return publicDesc;
        } else if (!description.equals("")) {
            return description;
        } else {
            return title;
        }
    }

    private void setDescription(boolean scroll){
        String description_pref = getDescription();
        TextView description_text = (TextView) findViewById(R.id.description_text);
        final ScrollView description_scroll = (ScrollView) findViewById(R.id.description_scroll);
        final ImageView icon = (ImageView) findViewById(R.id.visibility_icon);

        toggleDescription = findViewById(R.id.visibility_toggle);
        toggleDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (description_scroll.getVisibility() == View.GONE) {
                    //Expand the view
                    description_scroll.setVisibility(View.VISIBLE);
                    icon.setBackgroundResource(R.drawable.ic_visibility_closed);
                } else {
                    //Collapse the view
                    description_scroll.setVisibility(View.GONE);
                    icon.setBackgroundResource(R.drawable.ic_visibility_open);
                }
            }
        });

        Log.i("Description",description_pref);

        if (!description_pref.equals("")) {
            description_text.setVisibility(View.VISIBLE);
            description_text.setText(description_pref);
            //if (!description_pref.equals(description_text.getText().toString())) {
                description_text.setText(description_pref);
            //}
        } else {
            description_text.setVisibility(View.GONE);
        }

        if (scroll) {
            description_scroll.smoothScrollTo(0, 0);
            listView.smoothScrollToPosition(0);
        }
    }

    /*@SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_profile) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        } else if (id == R.id.nav_favorites) {
            Intent i = new Intent(this, FavoriteActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            writeEventAnalytics("App","Logout");

            Toast.makeText( getBaseContext(), "Logged out", Toast.LENGTH_SHORT).show();
            LoginHelper.setLogOut(sharedpreferences, navView);
        } else if (id == R.id.nav_login) {
            writeEventAnalytics("App","Login");

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
    }*/
}
