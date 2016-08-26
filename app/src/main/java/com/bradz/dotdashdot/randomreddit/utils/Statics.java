package com.bradz.dotdashdot.randomreddit.utils;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mauve3 on 8/23/16.
 */
public class Statics {

    public static String SHAREDSETTINGS = "randomreddit.settings";
    public static String SHAREDSETTINGS_AUTOUPDATE = "auto";

    public static String CURRENTSUB = "randomreddit.currentsub";
    public static String CURRENTSUB_DISPLAYNAME = "display_name";
    public static String CURRENTSUB_PUBLICDESCRIPTION = "public_description";
    public static String CURRENTSUB_DESCRIPTION = "description";
    public static String CURRENTSUB_TITLE = "title";
    public static String CURRENTSUB_HEADERTITLE = "header_title";
    public static String CURRENTSUB_NSFW = "nsfw";


    public static String CONTENTPROVIIDER = "com.bradz.dotdashdot.randomreddit.StockPriceContentProvider";

    private static Context self;
    public static Map<Integer, String> stringMap;

    public Statics(Context context, int[] keys) {
        self = context;
        stringMap = new HashMap<>();
        for (int key : keys) {
            stringMap.put(key,self.getResources().getString(key));
        }
        //String mystring = context.getResources().getString(R.string.mystring);
    }
    //String mystring = getResources().getString(R.string.mystring);
    //public static String CONTENT_PROVIDER = "com.bradz.dotdashdot.randomreddit.StockPriceContentProvider";
}
