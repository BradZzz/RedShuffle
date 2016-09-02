package com.bradz.dotdashdot.randomreddit.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.vending.billing.IInAppBillingService;

/**
 * Created by Mauve3 on 8/29/16.
 */
public class ParentApplication extends Application {

    private static IInAppBillingService mService;
    private static ServiceConnection mServiceConn;

    public void onCreate() {
        super.onCreate();

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    public IInAppBillingService getBillingConnection(){
        return mService;
    }

    public void onTerminate(){
        super.onTerminate();
        unbindService(mServiceConn);
    }
}
