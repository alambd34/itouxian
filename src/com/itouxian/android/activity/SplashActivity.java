package com.itouxian.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.flurry.android.FlurryAgent;
import com.itouxian.android.FileCache;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.util.FileUtils;
import com.itouxian.android.util.Utils;
import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

            String channels = PrefsUtil.getChannels();
            if (TextUtils.isEmpty(channels)) {
                sendChannels();
            } else {
                String[] strs = channels.split("\\|");
                int lastCode = Integer.parseInt(strs[1]);
                int versionCode = Utils.getVersionCode(SplashActivity.this);

                if (versionCode > lastCode) {
                    sendChannels();
                }
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

    private void sendChannels() {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);

            if (null != appInfo.metaData) {
                String channel = appInfo.metaData.getString("CHANNEL");

                Map<String, String> params = new HashMap<String, String>();
                params.put("name", channel);
                params.put("version", Utils.getVersionName(this));

                FlurryAgent.logEvent("CHANNEL", params);

                int versionCode = Utils.getVersionCode(this);
                PrefsUtil.saveChannels(channel + "|" + versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
    }
}
