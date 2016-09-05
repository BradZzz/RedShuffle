package com.bradz.dotdashdot.randomreddit.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mauve3 on 8/23/16.
 */
public class Statics {

    //Sync adapter
    public static final String SYNC_ACCOUNT = "default_account";
    public static final String SYNC_ACCOUNT_TYPE = "example.com";

    //Shared Preferences
    public static String SHAREDSETTINGS = "randomreddit.settings";
    public static String SHAREDSETTINGS_AUTOUPDATE = "auto";

    //Reddit Shared Preferences
    public static String SHAREDSETTINGS_REDDITACCESSTOKEN = "reddit_access_token";
    public static String SHAREDSETTINGS_REDDITEXPIRES = "reddit_token_expires";
    public static String SHAREDSETTINGS_REDDITREFRESHTOKEN = "reddit_refresh_token";

    //DB Tables
    public static String CURRENTSUB = "randomreddit.currentsub";
    public static String CURRENTSUB_DISPLAYNAME = "display_name";
    public static String CURRENTSUB_PUBLICDESCRIPTION = "public_description";
    public static String CURRENTSUB_DESCRIPTION = "description";
    public static String CURRENTSUB_TITLE = "title";
    public static String CURRENTSUB_SUBSCRIBERS = "subscribers";
    public static String CURRENTSUB_HEADERTITLE = "header_title";
    public static String CURRENTSUB_NSFW = "nsfw";

    //Google Billing
    public static String PURCHASES_DONATION_DOLLAR = "donation_1.00";
    public static String DEVELOPER_CALLBACK_ID = "ThisIsMySuperSecretCallback";

    //RedditOauth
    public static String REDDIT_BASE_URL = "https://www.reddit.com/api/v1/authorize.compact";
    public static String REDDIT_CLIENT_ID = "uiSl9J06xcbGlw";
    public static String REDDIT_REDIRECT_URL = "https://immersiveandroid.com/callback";
    public static String REDDIT_SCOPE = "mysubreddits subscribe account identity";

    //RedditApi
    public static String REDDIT_API_BASEURL = "https://oauth.reddit.com";
    public static String REDDIT_API_PROFILEURL = "/api/v1/me";
    public static String REDDIT_API_SUBREDDITSURL = "/subreddits/mine.json";
    public static String REDDIT_API_SUBREDDITSUBSCRIBE = "/api/subscribe";

    //RequestCodes
    public static int REDDIT_LOGIN1 = 1337;
    public static int REDDIT_PROFILE = 1400;
    public static int REDDIT_PROFILE_PREFS = 1401;
    public static int REDDIT_SUBREDDITS = 1500;
    public static int REDDIT_SUBREDDIT_SUBSCRIBE = 1501;
    public static int REDDIT_SUBREDDIT_UNSUBSCRIBE = 1502;

    public static String CONTENTPROVIIDER = "com.bradz.dotdashdot.randomreddit.StockPriceContentProvider";

    private static Context self;
    public static Map<Integer, String> stringMap;

    public Statics(Context context, int[] keys) {
        self = context;
        stringMap = new HashMap<>();
        for (int key : keys) {
            stringMap.put(key,self.getResources().getString(key));
        }
    }
}
