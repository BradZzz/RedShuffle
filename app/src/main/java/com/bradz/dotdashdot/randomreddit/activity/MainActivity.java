package com.bradz.dotdashdot.randomreddit.activity;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.adapters.SwipeAdapter;
import com.bradz.dotdashdot.randomreddit.application.ParentApplication;
import com.bradz.dotdashdot.randomreddit.helpers.StockDBHelper;
import com.bradz.dotdashdot.randomreddit.models.Thread;
import com.bradz.dotdashdot.randomreddit.routes.StockPriceContentProvider;
import com.bradz.dotdashdot.randomreddit.services.RequestService;
import com.bradz.dotdashdot.randomreddit.utils.Statics;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends NavigationActivity {
  private String TAG = this.getClass().getCanonicalName();
  private SwipeAdapter mSwipeAdapter;
  private ContentResolver mResolver;
  private Context self;
  private TextView mUpdatedTextView;
  private AVLoadingIndicatorView load_icon;
  private final static int PERMISSIONS_CALLBACK = 12321;

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

    sharedpreferences = getSharedPreferences(Statics.SHAREDSETTINGS, Context.MODE_PRIVATE);
    subredditpreferences = getSharedPreferences(Statics.CURRENTSUB, Context.MODE_PRIVATE);
    checkPermissions();
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

  private void checkPermissions() {
    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
      || (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
      || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SYNC_SETTINGS) != PackageManager.PERMISSION_GRANTED)
      || (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SYNC_SETTINGS) != PackageManager.PERMISSION_GRANTED)) {
      ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
          Manifest.permission.INTERNET,
          Manifest.permission.READ_SYNC_SETTINGS,
          Manifest.permission.WRITE_SYNC_SETTINGS},
        PERMISSIONS_CALLBACK);
    } else {
      initMain();
    }
  }

  private void initMain() {
    ParentApplication application = (ParentApplication) getApplication();
    mTracker = application.getDefaultTracker();
    initToolbar();
    initWidgets();
    initActionBar();
    mApp = ((ParentApplication) getApplicationContext());
    mResolver = getContentResolver();
    mAccount = createSyncAccount(this);
    getContentResolver().registerContentObserver(StockPriceContentProvider.CONTENT_URI, true, new StockContentObserver(new Handler()));
    Cursor existingStocksCursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI, null, null, null, null);
    tView = ((TextView) titleToolbar.findViewById(R.id.title));
    listView = (ListView) findViewById(R.id.stock_price_list);
    mSwipeAdapter = new SwipeAdapter(this, cursorToList(existingStocksCursor));
    listView.setAdapter(mSwipeAdapter);
    mSwipeAdapter.setMode(Attributes.Mode.Single);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((SwipeLayout)(listView.getChildAt(position - listView.getFirstVisiblePosition()))).open(true);
      }
    });

    listView.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView absListView, int i) {
      }

      @Override
      public void onScroll(AbsListView view,
                           int firstVisibleItem, int visibleItemCount,
                           int totalItemCount) {
        //Algorithm to check if the last item is visible or not
        final int lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount) {
          load_icon.setVisibility(View.VISIBLE);
          Bundle settingsBundle = new Bundle();
          settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
          settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
          String sub = subredditpreferences.getString(Statics.CURRENTSUB_DISPLAYNAME, "");
          String after = subredditpreferences.getString(Statics.CURRENTSUB_NEXT, "");
          if (!sub.equals("")) {
            settingsBundle.putString("subreddit", sub);
            if (!after.equals("")) {
              settingsBundle.putString("after", after);
            }
            ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, false);
            ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
          }
        }
      }
    });
    setDate(true);
    decideSync();

    if (existingStocksCursor.getCount() < 1) {
      reroll();
    }
  }

  private List<Thread> cursorToList(Cursor existingStocksCursor){
    List<Thread> thread = new ArrayList<Thread>();
    for (int i = 0; i < existingStocksCursor.getCount(); i++) {
      existingStocksCursor.moveToPosition(i);
      String title = existingStocksCursor.getString(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_TITLE));
      final String url = existingStocksCursor.getString(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_URL));
      String image = existingStocksCursor.getString(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_IMAGE));
      String sub = existingStocksCursor.getString(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_SUB));
      String full_image = existingStocksCursor.getString(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_FULL_IMAGE));
      int votes = existingStocksCursor.getInt(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_VOTES));
      int nsfw = existingStocksCursor.getInt(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_NSFW));
      final String comment = existingStocksCursor.getString(existingStocksCursor.getColumnIndex(StockDBHelper.COLUMN_COMMENTS));

      thread.add(new Thread(title, url, image, votes, full_image, comment, nsfw == 1));

      if (first_image == null && image != null && !image.isEmpty() && image.contains("http")) {
        first_image = image;
      }
      String subby = "/" + sub;
      if (tView != null && !tView.getText().equals(subby)) {
        tView.setText(subby);
        writeEventAnalytics("Sub", "New", sub);
      }
    }
    return thread;
  }

  private void initActionBar() {
    if (getActionBar() != null) {
      getActionBar().setDisplayShowTitleEnabled(false);
      getActionBar().setDisplayShowCustomEnabled(true);

      LayoutInflater inflator = LayoutInflater.from(this);
      titleToolbar = inflator.inflate(R.layout.titleview, null);
      TextView tView = ((TextView) titleToolbar.findViewById(R.id.title));
      tView.setText("Loading");
      getActionBar().setCustomView(titleToolbar);
    }
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayShowCustomEnabled(true);

      LayoutInflater inflator = LayoutInflater.from(this);
      titleToolbar = inflator.inflate(R.layout.titleview, null);
      TextView tView = ((TextView) titleToolbar.findViewById(R.id.title));
      tView.setText("Loading...");
      getSupportActionBar().setCustomView(titleToolbar);
    }
  }

  private void decideSync() {
    Boolean checked = sharedpreferences.getBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE, false);
    Log.i(TAG, "Checking: " + checked);
    if (checked) {
      startSync();
    } else {
      stopSync();
    }
  }

  private void startSync() {
    Log.i(TAG, "startSync");
    ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
    ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
    ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, 60);
  }

  private void stopSync() {
    Log.i(TAG, "stopSync");
    ContentResolver.setIsSyncable(mAccount, AUTHORITY, 0);
    ContentResolver.cancelSync(mAccount, AUTHORITY);
  }

  private void initWidgets() {
    load_icon = (AVLoadingIndicatorView) findViewById(R.id.load_indicator);
    load_icon.setVisibility(View.GONE);

    SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.switch_compat);
    switchCompat.setChecked(sharedpreferences.getBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE, false));
    switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Log.i(TAG, "Checked Change");
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(Statics.SHAREDSETTINGS_AUTOUPDATE, isChecked);
        editor.apply();

        Toast.makeText(getApplicationContext(), "Auto Update Enabled: " + isChecked, Toast.LENGTH_SHORT).show();

        decideSync();
      }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_CALLBACK: {
        if (grantResults.length > 0 && checkResults(grantResults)) {
          initMain();
        } else {
          Toast.makeText(this, "Permissions Denied. Closing App", Toast.LENGTH_LONG);
        }
      }
    }
  }

  private boolean checkResults(int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void initToolbar() {
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

          writeEventAnalytics("Sub", "Fav", sub);

          contentValues.put(StockDBHelper.COLUMN_SUB, sub);
          if (first_image != null) {
            contentValues.put(StockDBHelper.COLUMN_IMAGE, first_image);
          }

          //contentValues.put(StockDBHelper.COLUMN_FAVORITE, 1);
          contentValues.put(StockDBHelper.COLUMN_SEEN, 1);

          String title = subredditpreferences.getString(Statics.CURRENTSUB_TITLE, "");
          contentValues.put(StockDBHelper.COLUMN_DESCRIPTION, title);

                /*String publicDesc = getDescription();
                if (!publicDesc.equals("")) {
                    contentValues.put(StockDBHelper.COLUMN_DESCRIPTION, publicDesc);
                }*/

          Boolean nsfw = subredditpreferences.getBoolean(Statics.CURRENTSUB_NSFW, false);
          contentValues.put(StockDBHelper.COLUMN_NSFW, nsfw);

          int subscribers = subredditpreferences.getInt(Statics.CURRENTSUB_SUBSCRIBERS, 0);
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
        reroll();

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

  private void reroll() {
    Toast.makeText(getApplicationContext(), "Rerolling...", Toast.LENGTH_SHORT).show();

    writeEventAnalytics("App", "Reroll");

    ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
    ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, false);

    Bundle settingsBundle = new Bundle();
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
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
      Log.d(MainActivity.class.getName(), "Change observed at " + uri);

      Cursor cursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI, null, null, null, null);
      Log.d(MainActivity.class.getName(), "Cursor count: " + cursor.getCount());
      if (cursor.getCount() > 27) {
        setDate(false);
      } else {
        setDate(true);
      }
      mSwipeAdapter.swapList(cursorToList(cursor));
      mSwipeAdapter.notifyDataSetChanged();
    }
  }

  private void setDate(boolean scroll) {
    first_image = null;
    mUpdatedTextView = (TextView) findViewById(R.id.updated_text);
    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
    mUpdatedTextView.setText("Last updated: " + currentDateTimeString);
    setDescription(scroll);
  }

  private String getDescription() {
    String publicDesc = subredditpreferences.getString(Statics.CURRENTSUB_PUBLICDESCRIPTION, "");
    String description = subredditpreferences.getString(Statics.CURRENTSUB_DESCRIPTION, "");
    String title = subredditpreferences.getString(Statics.CURRENTSUB_TITLE, "");

    Log.i("getDescription", "publicDesc: " + publicDesc);
    Log.i("getDescription", "description: " + description);
    Log.i("getDescription", "title: " + title);

    if (!publicDesc.equals("")) {
      return publicDesc;
    } else if (!description.equals("")) {
      return description;
    } else {
      return title;
    }
  }

  private void setDescription(boolean scroll) {
    String description_pref = getDescription();
    TextView description_text = (TextView) findViewById(R.id.description_text);
    final LinearLayout description_parent = (LinearLayout) findViewById(R.id.description_parent);
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
        //description_parent.invalidate();
      }
    });

    Log.i("Description", description_pref);

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
}
