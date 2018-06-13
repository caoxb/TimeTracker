package com.example;

import android.app.Application;
import android.os.StrictMode;

import com.example.utils.Util;

public class TimeTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //总感觉开始的严格模式不对
        if (Util.useStrictMode(this)) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .penaltyDeath()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectActivityLeaks()
                    .penaltyDeath()
                    .build());
        }
    }
}
