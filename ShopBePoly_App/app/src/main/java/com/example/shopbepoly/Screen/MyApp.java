package com.example.shopbepoly.Screen;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.shopbepoly.API.WebSocketManager;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
    }
}
