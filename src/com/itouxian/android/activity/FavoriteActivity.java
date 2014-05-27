package com.itouxian.android.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;

/**
 * Created by chenjishi on 14-5-27.
 */
public class FavoriteActivity extends BaseActivity {
    private String mToken;
    private int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        mToken = PrefsUtil.getUser().token;

        Bundle bundle = new Bundle();
        bundle.putInt(FeedListFragment.BUNDLE_KEY_TYPE, FeedListFragment.FEED_LIST_FAVORITE);
        FeedListFragment fragment = (FeedListFragment) Fragment.instantiate(this,
                FeedListFragment.class.getName(), bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.commit();
    }
}
