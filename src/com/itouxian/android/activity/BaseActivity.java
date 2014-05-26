package com.itouxian.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.util.Constants;

import static com.itouxian.android.util.Constants.*;

/**
 * Created by chenjishi on 14-2-15.
 */
public class BaseActivity extends FragmentActivity {
    protected FrameLayout mRootView;
    protected LayoutInflater mInflater;

    protected int mTitleResId = -1;
    protected float mDensity;
    protected boolean mHideTitle;

    protected int mTheme;

    private ThemeChangeReceiver mThemeReceiver;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_layout);

        mDensity = getResources().getDisplayMetrics().density;
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mRootView = (FrameLayout) findViewById(android.R.id.content);
        mRootView.setBackgroundColor(getResources().getColor(MODE_NIGHT == PrefsUtil.getThemeMode()
                ? R.color.background_night : R.color.background));

        if (!mHideTitle) {
            final int resId = -1 == mTitleResId ? R.layout.base_title_layout : mTitleResId;
            mInflater.inflate(resId, mRootView);
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
        final int marginTop = mHideTitle ? 0 : (int) (mDensity * 48 + .5f);
        layoutParams.setMargins(0, marginTop, 0, 0);
        View contentView = mInflater.inflate(layoutResID, null);
        mRootView.addView(contentView, layoutParams);
    }

    protected void setContentView(int layoutResID, int titleResId) {
        mTitleResId = titleResId;
        setContentView(layoutResID);
    }

    protected void setContentView(int layoutResID, boolean hideTitle) {
        mHideTitle = hideTitle;
        setContentView(layoutResID);
    }

    @Override
    public void setTitle(CharSequence title) {
        final TextView textView = (TextView) findViewById(R.id.title_text);
        textView.setText(title);
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    protected void setRightButtonIcon(int resId) {
        final ImageButton button = (ImageButton) findViewById(R.id.btn_right);
        button.setImageResource(resId);
        button.setVisibility(View.VISIBLE);
    }

    public void onLeftViewClicked(View view) {
        finish();
    }

    public void onRightButtonClicked(View view) {

    }

    protected int dp2px(int n) {
        return (int) (n * mDensity + .5f);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTheme = PrefsUtil.getThemeMode();
        applyTheme(mTheme);
        IntentFilter filter = new IntentFilter(Constants.ACTION_THEME_CHANGED);
        mThemeReceiver = new ThemeChangeReceiver();
        registerReceiver(mThemeReceiver, filter);
        FlurryAgent.onStartSession(this, "ZZF7PQQ23BYVC9V9TXW3");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mThemeReceiver);
        FlurryAgent.onEndSession(this);
    }

    protected void applyTheme(int theme) {

    }

    private class ThemeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_THEME_CHANGED)) {
                int mode = intent.getIntExtra(KEY_THEME_MODE, MODE_DAY);
                applyTheme(mode);
            }
        }
    }
}
