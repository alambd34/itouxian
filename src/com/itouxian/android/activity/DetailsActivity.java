package com.itouxian.android.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.util.DepthPageTransformer;

import static com.itouxian.android.util.Constants.BUNDLE_KEY_FEED;
import static com.itouxian.android.util.Constants.KEY_FEED_LIST;
import static com.itouxian.android.util.Constants.KEY_FEED_INDEX;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-5-30.
 */
public class DetailsActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private ArrayList<Feed> mFeedList;

    private ViewPager mViewPager;
    private FeedPagerAdapter mPagerAdapter;

    private int mCurrentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Bundle bundle = getIntent().getExtras();
        mFeedList = bundle.getParcelableArrayList(KEY_FEED_LIST);
        mCurrentIndex = bundle.getInt(KEY_FEED_INDEX, 0);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new FeedPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(mCurrentIndex);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setPageMarginDrawable(R.drawable.sliding_back_shadow);
        mViewPager.setPageMargin(dp2px(8));
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

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
}
