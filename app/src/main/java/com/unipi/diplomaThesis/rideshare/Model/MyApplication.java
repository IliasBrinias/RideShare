package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
    private ActivityLifecycle activityLifecycle = new ActivityLifecycle();
    @Override
    public void onCreate() {
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        super.onCreate();
        registerActivityLifecycleCallbacks(activityLifecycle);
    }
    public Activity getActiveActivity(){
        return activityLifecycle.getCurrentActivity();
    }
}
