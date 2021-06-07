package com.lipisoft.toyshark.application;

import android.app.Application;

public class MyApplication extends Application {

    public static MyApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
