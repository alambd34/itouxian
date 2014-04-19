package com.itouxian.android;

import android.app.Application;
import com.itouxian.android.util.HttpUtils;

/**
 * Created by chenjishi on 14-3-15.
 */
public class App extends Application {
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        HttpUtils.init(this);
    }

    public static App getInstance() {
        return mInstance;
    }
}
