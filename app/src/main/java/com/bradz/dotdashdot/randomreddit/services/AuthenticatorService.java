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
        // Create a new authenticator object
        mAuthenticator = new FakeAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
