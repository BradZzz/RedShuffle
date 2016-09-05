package com.bradz.dotdashdot.randomreddit.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bradz.dotdashdot.randomreddit.helpers.FakeAuthenticator;

/**
 * Created by drewmahrt on 3/7/16.
 */
public class AuthenticatorService extends Service {
    private FakeAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        mAuthenticator = new FakeAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
