package com.bradz.dotdashdot.randomreddit.helpers;

import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.utils.Statics;

/**
 * Created by Mauve3 on 9/1/16.
 */
public class LoginHelper {
    public static  boolean checkLogin(SharedPreferences sharedpreferences, NavigationView navView){
        Long currentTime  = System.currentTimeMillis();
        Long expires = sharedpreferences.getLong(Statics.SHAREDSETTINGS_REDDITEXPIRES, currentTime);
        if (currentTime > expires) {
            setLogIn(navView);
            return true;
        } else {
            setLogOut(sharedpreferences, navView);
            return false;
        }
    }

    public static void setLogIn(NavigationView navView){
        navView.getMenu().findItem(R.id.nav_login).setVisible(false);
        navView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        navView.getMenu().findItem(R.id.nav_profile).setVisible(true);
    }

    public static void setLogOut(SharedPreferences sharedpreferences, NavigationView navView){
        sharedpreferences.edit().remove(Statics.SHAREDSETTINGS_REDDITACCESSTOKEN).apply();
        sharedpreferences.edit().remove(Statics.SHAREDSETTINGS_REDDITEXPIRES).apply();
        sharedpreferences.edit().remove(Statics.SHAREDSETTINGS_REDDITREFRESHTOKEN).apply();

        navView.getMenu().findItem(R.id.nav_login).setVisible(true);
        navView.getMenu().findItem(R.id.nav_logout).setVisible(false);
        navView.getMenu().findItem(R.id.nav_profile).setVisible(false);
    }
}
