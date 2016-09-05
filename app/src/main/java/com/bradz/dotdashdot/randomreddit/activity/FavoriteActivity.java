package com.bradz.dotdashdot.randomreddit.activity;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FavoriteActivity extends NavigationActivity {

    public final Handler profileHandler = new Handler() {

        public void handleMessage(Message msg) {

            Log.i("ProfileActivity","Message handled here!");

            int type = msg.getData().getInt("type");
            //long request_time = msg.getData().getLong("request_time");
            String response = msg.getData().getString("response");
            Boolean error = msg.getData().getBoolean("error");

            Log.i("responseHandler","Type: " + type);
            Log.i("responseHandler","Response: " + response);
            Log.i("responseHandler","Error: " + error);

            //If the user has logged into the app, save the token into shared preferences
            if (Statics.REDDIT_SUBREDDIT_SUBSCRIBE == type && !error) {
                Toast.makeText(getApplicationContext(), "Subscribed to subreddit!", Toast.LENGTH_SHORT).show();
            } else if (Statics.REDDIT_SUBREDDIT_SUBSCRIBE == type && error) {
                Toast.makeText(getApplicationContext(), "Error subscribing to subreddit!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Context self;
    private CursorAdapter mCursorAdapter;
    ContentResolver mResolver;
    private String TAG = this.getClass().getCanonicalName();
    public static final String AUTHORITY = Statics.CONTENTPROVIIDER;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        self = this;

        mAccount = createSyncAccount(this);

        initToolbar();
        mResolver = getContentResolver();

        mApp = ((ParentApplication) getApplicationContext());
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
                        final Map<String, String> params = new HashMap<>();
                        params.put("action", "sub");
                        params.put("sr_name", subreddit);

                        final Map<String,String> headers = new HashMap<>();
                        String access_token = sharedpreferences.getString(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN,"");
                        if (!access_token.equals("")) {
                            headers.put("Authorization", "bearer " + access_token);

                            Log.i("Favorites","Sub url: " + Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_SUBREDDITSUBSCRIBE);

                            mRequestService.createPostRequest(self, profileHandler,
                                    Statics.REDDIT_API_BASEURL + Statics.REDDIT_API_SUBREDDITSUBSCRIBE, params, headers, Statics.REDDIT_SUBREDDIT_SUBSCRIBE);
                        } else {
                            Toast.makeText(getBaseContext(), "Please login to use the subscribe feature", Toast.LENGTH_SHORT).show();
                        }
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
