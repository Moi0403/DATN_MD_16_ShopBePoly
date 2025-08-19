package com.example.shopbepoly.Screen;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.example.shopbepoly.API.WebSocketManager;

public class AppLifecycleListener implements LifecycleObserver {

//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    public void onAppForegrounded() {
//        Log.d("AppLifecycle", "App came to foreground");
//        WebSocketManager.reconnect(); // Tự động kết nối lại
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    public void onAppBackgrounded() {
//        Log.d("AppLifecycle", "App went to background");
//        WebSocketManager.disconnect(); // Ngắt kết nối
    //    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        Log.d("AppLifecycle", "App came to foreground");
        if (!WebSocketManager.isConnected()) {
            WebSocketManager.reconnect();
        }
    }

}
