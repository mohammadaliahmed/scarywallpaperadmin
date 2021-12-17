package com.eaw.wallpaperadmin.Utils;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.google.firebase.FirebaseApp;


/**
 * Created by AliAh on 29/03/2018.
 */

public class ApplicationClass extends Application {
    private static ApplicationClass instance;


    public static ApplicationClass getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
