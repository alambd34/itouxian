package com.itouxian.android.activity;

import android.os.Bundle;
import android.util.Log;
import com.itouxian.android.R;

import static com.itouxian.android.util.IntentUtils.KEY_FEED_ID;

/**
 * Created by chenjishi on 14-4-2.
 */
public class CommentActivity extends SlidingActivity {
    private final String REQUEST_URL = "http://www.itouxian.com/json/get_comment/%1$d/%2$d";
    private long mFeedId;
    private int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mFeedId = getIntent().getExtras().getLong(KEY_FEED_ID);
        if (mFeedId == 0) finish();

        String url = String.format(REQUEST_URL, mFeedId, mPage);
        Log.i("test", url);
    }
}
