package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private Activity currentActivity;

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostStopped(activity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostResumed(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
