package com.itouxian.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.IntentUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.LoginDialog;
import org.json.JSONException;
import org.json.JSONObject;
import volley.Response;
import volley.VolleyError;

import java.util.ArrayList;
import java.util.HashMap;

import static com.itouxian.android.util.Constants.*;

/**
 * Created by chenjishi on 14-5-30.
 */
public class DetailsActivity extends BaseActivity implements ViewPager.OnPageChangeListener, LoginDialog.OnLoginListener {
    private ArrayList<Feed> mFeedList;
    private static final int LOGIN_COMMENT_CLICK = 100;
    private static final int LOGIN_VOTE_CLICK = 101;
    private static final int LOGIN_FAVORITE_CLICK = 102;
    private int mLoginClickType;

    private ViewPager mViewPager;
    private FeedPagerAdapter mPagerAdapter;

    private int mCurrentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details, R.layout.details_title_layout);
        Bundle bundle = getIntent().getExtras();
        mFeedList = bundle.getParcelableArrayList(KEY_FEED_LIST);
        mCurrentIndex = bundle.getInt(KEY_FEED_INDEX, 0);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new FeedPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(mCurrentIndex);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        mCurrentIndex = i;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onLoginSuccess() {
        Feed feed = mFeedList.get(mCurrentIndex);
        switch (mLoginClickType) {
            case LOGIN_COMMENT_CLICK:
                startCommentActivity(feed.id);
                break;
            case LOGIN_VOTE_CLICK:
//                vote(mClickedItemId);
                break;
            case LOGIN_FAVORITE_CLICK:
                favorite(feed.id);
                break;
        }
    }

    @Override
    public void onLoginError() {

    }

    public void onFavoriteClicked(View view) {
        Feed feed = mFeedList.get(mCurrentIndex);
        if (Utils.isLogin()) {
            favorite(feed.id);
        } else {
            mLoginClickType = LOGIN_FAVORITE_CLICK;
            new LoginDialog(this, this).show();
        }

    }

    private void favorite(long feedId) {
        String token = PrefsUtil.getUser().token;

        String url = "http://www.itouxian.com/json/favourite";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(feedId));
        params.put("token", token);

        HttpUtils.post(url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int code = -1;
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        code = jsonObj.optInt("code", -1);
                    } catch (JSONException e) {
                    }
                }
                Utils.showToast(code == 0 ? R.string.favorite_success : R.string.favorite_fail);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(R.string.favorite_fail);
            }
        });
    }

    public void onCommentClicked(View view) {
        Feed feed = mFeedList.get(mCurrentIndex);

        if (Utils.isLogin()) {
            startCommentActivity(feed.id);
        } else {
            mLoginClickType = LOGIN_COMMENT_CLICK;
            new LoginDialog(this, this).show();
        }
    }

    public void onShareClicked(View view) {
        Feed feed = mFeedList.get(mCurrentIndex);

        String imageUrl = feed.imageUrl;
        if (!TextUtils.isEmpty(imageUrl)) {
            Utils.shareImage(this, imageUrl);
        } else {
            Utils.shareText(this, feed.contents);
        }
    }

    private void startCommentActivity(long id) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra(IntentUtils.KEY_FEED_ID, id);
        startActivity(intent);
    }

    private class FeedPagerAdapter extends FragmentStatePagerAdapter {
        public FeedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            final Feed feed = mFeedList.get(i);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BUNDLE_KEY_FEED, feed);
            return Fragment.instantiate(DetailsActivity.this, DetailsFragment.class.getName(), bundle);
        }

        @Override
        public int getCount() {
            return null != mFeedList ? mFeedList.size() : 0;
        }
    }

    @Override
    protected void applyTheme(int theme) {
        super.applyTheme(theme);

        ImageView commentBtn = (ImageView) findViewById(R.id.ic_comment);
        ImageButton shareBtn = (ImageButton) findViewById(R.id.btn_share);
        ImageButton favoriteBtn = (ImageButton) findViewById(R.id.btn_favorite);

        if (MODE_NIGHT == theme) {
            commentBtn.setImageResource(R.drawable.ic_comment_night);
            shareBtn.setImageResource(R.drawable.ic_share_night);
            favoriteBtn.setImageResource(R.drawable.ic_favorite_night);
        } else {
            commentBtn.setImageResource(R.drawable.ic_comment);
            shareBtn.setImageResource(R.drawable.ic_social_share);
            favoriteBtn.setImageResource(R.drawable.ic_favorite2);
        }
    }
}