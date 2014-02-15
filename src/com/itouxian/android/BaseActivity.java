package com.itouxian.android;

import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;

/**
 * Created by chenjishi on 14-2-15.
 */
public class BaseActivity extends FragmentActivity implements SlidingPaneLayout.PanelSlideListener {
    private FrameLayout rootView;
    private SlidingPaneLayout slidingPane;

    private boolean hideTitle = false;
    private int titleResId = -1;
    protected int theme;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_layout);

        rootView = (FrameLayout) findViewById(R.id.content_view);
//        rootView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == PrefsUtil.getThemeMode()
//                ? R.color.background_night : R.color.background));
        rootView.setBackgroundColor(getResources().getColor(R.color.background));

        if (!hideTitle) {
            int resId = -1 == titleResId ? R.layout.base_title_layout : titleResId;
            LayoutInflater.from(this).inflate(resId, rootView);
        }

        View contentView = LayoutInflater.from(this).inflate(layoutResID, null);
        contentView.setId(R.id.content_layout);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
        int marginTop = hideTitle ? 0 : (int) getResources().getDimension(R.dimen.action_bar_height);
        lp.setMargins(0, marginTop, 0, 0);
        rootView.addView(contentView, lp);

        slidingPane = (SlidingPaneLayout) findViewById(R.id.slide_panel);
        slidingPane.setShadowResource(R.drawable.sliding_back_shadow);
        slidingPane.setSliderFadeColor(0x00000000);
        slidingPane.setPanelSlideListener(this);
    }

    protected void setContentView(int layoutResID, int titleResId) {
        this.titleResId = titleResId;
        setContentView(layoutResID);
    }

    protected void setContentView(int layoutResID, boolean hideTitle) {
        this.hideTitle = hideTitle;
        setContentView(layoutResID);
    }

    @Override
    public void setTitle(CharSequence title) {
        final TextView textView = (TextView) findViewById(R.id.tv_title);
        textView.setText(title);
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "ZZF7PQQ23BYVC9V9TXW3");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onPanelSlide(View view, float v) {
        if (v >= 0.9) finish();
    }

    @Override
    public void onPanelOpened(View view) {

    }

    @Override
    public void onPanelClosed(View view) {

    }
}
