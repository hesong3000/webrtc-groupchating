package com.ichat.utility;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ichat.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fwj on 2016/7/13.
 */
public class ApplicationSession extends Application{
    private String TAG = ApplicationSession.class.getName();
    private User user = null;
    private List<Activity> activities = new ArrayList<Activity>();
    public List<Activity> getActivities() {
        return activities;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Context getContext(){
        return this.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate!");
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity){
        activities.remove(activity);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG,"onTerminate!");
        for (Activity activity : activities) {
            Log.d(TAG,activity.getClass().getName() + " finish!" );
            activity.finish();
        }

        System.exit(0);
    }

}
