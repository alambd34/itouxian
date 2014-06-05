package com.itouxian.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.itouxian.android.FileCache;
import com.itouxian.android.R;
import com.itouxian.android.util.FileUtils;
import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotManager;

import java.io.File;

/**
 * Created by chenjishi on 14-2-15.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.background));

        new LoadTask().execute();
    }

    private class LoadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            AdManager.getInstance(SplashActivity.this).init("3ed888768e6b2670", "b3360a140921b694", false);
            SpotManager.getInstance(SplashActivity.this).loadSpotAds();
            SpotManager.getInstance(SplashActivity.this).setSpotTimeout(10000);

            //clear temp files, such as shared image or temp upgrade apk
            File tempFile = new File(FileCache.getTempCacheDir());
            if (tempFile.exists()) {
                FileUtils.delete(tempFile);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Handler mainThread = new Handler(Looper.getMainLooper());
            mainThread.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }, 2000L);
        }
    }
}
