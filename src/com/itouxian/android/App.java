package com.itouxian.android;

import android.app.Application;
import com.itouxian.android.util.HttpUtils;

import java.io.File;

/**
 * Created by chenjishi on 14-3-15.
 */
public class App extends Application {
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        FileCache.init(this);
        HttpUtils.init(this);
    }

    public static App getInstance() {
        return mInstance;
    }
}
