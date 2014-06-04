package com.itouxian.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.itouxian.android.R;
import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotManager;

/**
 * Created by chenjishi on 14-2-15.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.background));

        AdManager.getInstance(this).init("3ed888768e6b2670", "b3360a140921b694", false);
        SpotManager.getInstance(this).loadSpotAds();
        SpotManager.getInstance(this).setSpotTimeout(10000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 3000L);
    }
}
