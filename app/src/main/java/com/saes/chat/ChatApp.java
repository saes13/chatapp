// ChatApp.java
package com.saes.chat;

import android.app.Application;
import android.util.Log;

public class ChatApp extends Application {

    private static boolean isAppInForeground = false;
    private static final String TAG = "ChatApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ChatApp inicializado");
    }

    public static void setAppInForeground(boolean isForeground) {
        isAppInForeground = isForeground;
        Log.d(TAG, "App en primer plano: " + isForeground);
    }

    public static boolean isAppInForeground() {
        return isAppInForeground;
    }
}