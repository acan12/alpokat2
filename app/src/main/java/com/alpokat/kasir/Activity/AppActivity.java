package com.alpokat.kasir.Activity;

import android.app.ActivityManager;
import android.content.Context;

import app.beelabs.com.codebase.base.BaseActivity;

public class AppActivity extends BaseActivity {

    protected boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
