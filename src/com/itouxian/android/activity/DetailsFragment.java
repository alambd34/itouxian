package com.itouxian.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.util.Constants;
import com.itouxian.android.util.JSCallback;
import com.itouxian.android.util.JavaScriptBridge;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.CircleView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.itouxian.android.util.Constants.BUNDLE_KEY_FEED;
import static com.itouxian.android.util.Constants.MODE_NIGHT;

/**
 * Created by chenjishi on 14-5-30.
 */
public class DetailsFragment extends Fragment implements JSCallback {
    private WebView mWebView;

    private Feed mFeed;
    private JavaScriptBridge mJsBridge;

    private int mThemeMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mFeed = bundle.getParcelable(BUNDLE_KEY_FEED);
        mJsBridge = new JavaScriptBridge(this);

        mThemeMode = PrefsUtil.getThemeMode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        view.setBackgroundColor(MODE_NIGHT == mThemeMode ? 0xFF222222 : 0xFFF0F0F0);

        mWebView = (WebView) view.findViewById(R.id.web_view);
        mWebView.setBackgroundColor(0x00000000);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.addJavascriptInterface(mJsBridge, "U148");

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderPage();
    }

    private void renderPage() {
        String template = Utils.readFromAssets(getActivity(), "usite.html");

        if (TextUtils.isEmpty(template)) return;

        template = template.replace("{TITLE}", mFeed.title);

        final UserInfo usr = mFeed.usr;
        if (null != usr) {
            long t = mFeed.create_time * 1000L;
            Date date = new Date(t);
            Format format = new SimpleDateFormat("yyyy-MM-dd");
            String pubTime = getString(R.string.pub_time, mFeed.usr.nickname,
                    format.format(date));
            template = template.replace("{U_AUTHOR}", pubTime);
            String reviews = getString(R.string.pub_reviews,mFeed.count_browse,mFeed.count_review);
            template = template.replace("{U_COMMENT}", reviews);
        } else {
            template = template.replace("{U_AUTHOR}", "");
            template = template.replace("{U_COMMENT}", "");
        }
        template = template.replace("{CONTENT}", mFeed.contents);
        template = template.replace("{SCREEN_MODE}", mThemeMode == MODE_NIGHT ? "night" : "");

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
    }

    @Override
    public void onImageClicked(String url) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(url);

        Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
        intent.putExtra("imgsrc", url);
        intent.putStringArrayListExtra("images", list);
        startActivity(intent);
    }

    @Override
    public void onMusicClicked(String url) {

    }

    @Override
    public void onVideoClicked(String url) {

    }

    @Override
    public void onThemeChange() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int mode = PrefsUtil.getThemeMode();
                if (mode == Constants.MODE_NIGHT) {
                    mWebView.loadUrl("javascript:setScreenMode('night')");
                } else {
                    mWebView.loadUrl("javascript:setScreenMode('day')");
                }
            }
        });
    }
}
