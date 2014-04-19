package com.itouxian.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.itouxian.android.R;

/**
 * Created by chenjishi on 14-2-15.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.background));
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
