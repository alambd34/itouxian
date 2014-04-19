package com.itouxian.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.ArticleWebView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenjishi on 14-4-2.
 */
public class DetailActivity extends SlidingActivity {
    private ArticleWebView mWebView;
    private JavascriptBridge mJsBridge;

    private Feed mFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = getIntent().getExtras();

        if (null != bundle) {
            mFeed = bundle.getParcelable("feed");
        }

        mWebView = (ArticleWebView) findViewById(R.id.webview_content);

        mJsBridge = new JavascriptBridge(this);
        mWebView.addJavascriptInterface(mJsBridge, "U148");

        renderPage();
    }

    private void renderPage() {
        String template = Utils.readFromAssets(this, "usite.html");
        template = template.replace("{TITLE}", mFeed.title);


        template = template.replace("{CONTENT}", mFeed.contents);

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
    }

    private class JavascriptBridge {
        private Context mContext;

        public JavascriptBridge(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void onImageClick(String src) {
//            Intent intent = new Intent(mContext, null);
//            intent.putExtra("imgsrc", src);
//            intent.putStringArrayListExtra("images", mArticle.imageList);
//            mContext.startActivity(intent);
        }
    }
}
