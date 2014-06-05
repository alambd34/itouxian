package com.itouxian.android.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.util.Constants;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.AboutDialog;
import com.itouxian.android.view.ExitDialog;
import com.itouxian.android.view.FireworksView;
import com.itouxian.android.view.LoginDialog;
import net.youmi.android.spot.SpotManager;
import volley.toolbox.ImageLoader;

import static com.itouxian.android.activity.FeedListFragment.*;
import static com.itouxian.android.util.ConstantUtil.MODE_DAY;
import static com.itouxian.android.util.ConstantUtil.MODE_NIGHT;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        DrawerLayout.DrawerListener, LoginDialog.OnLoginListener,
        ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener {
    private static final int LOGIN_FAVORITE_CLICK = 100;
    private static final int LOGIN_EDIT_CLICK = 101;
    public static final int REQUEST_CODE_REGISTER = 101;
    public static final int RESULT_CODE_REGISTER = 102;

    public static final int TAB_BUTTON_ID = 1000;

    private int mLoginClickType;

    private TextView mTitleText;
    private DrawerLayout mDrawerLayout;

    private ImageLoader mImageLoader;

    private FeedPagerAdapter mPagerAdapter;
    private RadioGroup mTabGroup;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main, R.layout.home_title_layout);
        setRightButtonIcon(R.drawable.ic_edit);

        mTitleText = (TextView) findViewById(R.id.title_text);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);

        mImageLoader = HttpUtils.getImageLoader();

        updateMenuList(Utils.isLogin());

        String[] titles = getResources().getStringArray(R.array.tab_titles);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(36));
        mTabGroup = new RadioGroup(this);
        mTabGroup.setLayoutParams(lp);
        mTabGroup.setBackgroundColor(0xFFE5E5E5);
        mTabGroup.setWeightSum(3.f);
        mTabGroup.setOrientation(LinearLayout.HORIZONTAL);
        mTabGroup.setPadding(dp2px(8), 0, dp2px(8), 0);
        container.addView(mTabGroup);

        int len = titles.length;
        for (int i = 0; i < len; i++) {
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            if (i < len - 1) {
                layoutParams.setMargins(0, 0, dp2px(8), 0);
            }

            RadioButton button = new RadioButton(this);
            button.setGravity(Gravity.CENTER);
            button.setText(titles[i]);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            button.setTextColor(0xFF333333);
            button.setButtonDrawable(new StateListDrawable());
            button.setBackgroundResource(R.drawable.abs__tab_indicator_ab_holo);
            button.setLayoutParams(layoutParams);
            button.setId(TAB_BUTTON_ID + i);

            mTabGroup.addView(button);
        }

        View divider = new View(this);
        divider.setBackgroundColor(0xFFCCCCCC);
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        container.addView(divider);

        LinearLayout.LayoutParams pagerLayoutParames = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPagerAdapter = new FeedPagerAdapter(getSupportFragmentManager());
        mViewPager = new ViewPager(this);
        /** When using FragmentPagerAdapter the host ViewPager must have a valid ID set. */
        mViewPager.setId(TAB_BUTTON_ID + 4);
        mViewPager.setLayoutParams(pagerLayoutParames);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setOnPageChangeListener(this);
        container.addView(mViewPager);

        mTabGroup.setOnCheckedChangeListener(this);
        mTabGroup.check(TAB_BUTTON_ID);

        mTitleText.postDelayed(new Runnable() {
            @Override
            public void run() {
                SpotManager.getInstance(MainActivity.this).showSpotAds(MainActivity.this);

            }
        }, 5000);
    }

    private LinearLayout mMenuContainer;
    private void updateMenuList(boolean isLogin) {
        mMenuContainer = (LinearLayout) findViewById(R.id.left_view);
        mMenuContainer.removeAllViews();

        int index = 0;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);

        LinearLayout loginLayout = new LinearLayout(this);
        int paddingLeft = dp2px(8);
        int paddingTop = dp2px(14);
        loginLayout.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
        loginLayout.setBackgroundResource(R.drawable.highlight_color);
        loginLayout.setOnClickListener(this);
        loginLayout.setId(R.id.menu_item + index);
        loginLayout.setLayoutParams(lp);
        mMenuContainer.addView(loginLayout);
        mMenuContainer.addView(generateDivider(), lp1);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.ic_avatar_2, options);

        ImageView userIcon = new ImageView(this);
        userIcon.setLayoutParams(new LinearLayout.LayoutParams(options.outWidth, options.outHeight));
        loginLayout.addView(userIcon);

        TextView loginText = generateView(R.string.login, -1, 0);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(paddingLeft, 0, 0, 0);
        loginLayout.addView(loginText, lp2);

        if (isLogin) {
            UserInfo user = PrefsUtil.getUser();
            mImageLoader.get(user.icon, ImageLoader.getImageListener(userIcon,
                    R.drawable.user_default, R.drawable.user_default));
            loginText.setText(user.nickname);
        } else {
            userIcon.setImageResource(R.drawable.user_default);
            loginText.setText(R.string.login);
        }

        if (!isLogin) {
            index += 1;
            mMenuContainer.addView(generateView(R.string.register, R.drawable.ic_avatar_2, index), lp);
            mMenuContainer.addView(generateDivider(), lp1);
        }

        int[] nameIds = {
                R.string.settings,
                PrefsUtil.getThemeMode() == MODE_DAY ? R.string.night : R.string.day,
                R.string.favorite,
                R.string.about};

        int[] iconIds = {
                R.drawable.ic_settings,
                R.drawable.ic_bulb,
                R.drawable.ic_favorite_menu,
                R.drawable.ic_info};


        int baseIndex = 3;
        for (int i = 0; i < nameIds.length; i++) {
            mMenuContainer.addView(generateView(nameIds[i], iconIds[i], baseIndex + i), lp);
            mMenuContainer.addView(generateDivider(), lp1);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mViewPager.setCurrentItem(checkedId - TAB_BUTTON_ID);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        mTabGroup.check(TAB_BUTTON_ID + i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private View generateDivider() {
        final View view = new View(this);
        view.setBackgroundColor(0xFF555555);
        return view;
    }

    private TextView generateView(int nameId, int iconId, int index) {
        final int paddingLeft = dp2px(8);
        final int paddingTop = dp2px(14);
        final TextView tv = new TextView(this);
        tv.setId(R.id.menu_item + index);
        tv.setTextColor(0xFFEEEEEE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.f);
        tv.setText(nameId);
        if (iconId > 0) {
            tv.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
            tv.setCompoundDrawablePadding(paddingLeft);
            tv.setBackgroundResource(R.drawable.highlight_color);
            tv.setOnClickListener(this);

            tv.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
        }

        return tv;
    }

    @Override
    public void onLeftViewClicked(View view) {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public void onDrawerSlide(View view, float v) {
        final int indent = (int) (v * dp2px(8));
        mTitleText.setPadding(-indent, 0, dp2px(8), 0);
    }

    @Override
    public void onDrawerOpened(View view) {
    }

    @Override
    public void onDrawerClosed(View view) {

    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public void onRightButtonClicked(View view) {
        if (Utils.isLogin()) {
            toPostActivity();
        } else {
            mLoginClickType = LOGIN_EDIT_CLICK;
            new LoginDialog(this, this).show();
        }
    }

    @Override
    public void onLoginSuccess() {
        switch (mLoginClickType) {
            case LOGIN_EDIT_CLICK:
                toPostActivity();
                break;
            case LOGIN_FAVORITE_CLICK:
                toFavoriteActivity();
                break;
        }
        updateMenuList(true);
    }

    @Override
    public void onLoginError() {
        Utils.showToast(getString(R.string.login_fail));
    }

    private void toPostActivity() {
        Intent intent = new Intent(this, FeedPostActivity.class);
        startActivity(intent);
    }

    private void toFavoriteActivity() {
        Intent intent = new Intent(this, FavoriteActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        int index = id - R.id.menu_item;
        switch (index) {
            case 0:
                UserInfo info = PrefsUtil.getUser();
                if (null != info && !TextUtils.isEmpty(info.token)) {
                    new ExitDialog(this, new ExitDialog.OnLogoutListener() {
                        @Override
                        public void logout() {
                            PrefsUtil.setUser(null);
                            updateMenuList(false);
                            Utils.showToast(R.string.logout_success);
                        }
                    }).show();
                } else {
                    new LoginDialog(this, this).show();
                }
                break;
            case 1:
                startActivityForResult(new Intent(this, RegisterActivity.class),
                        REQUEST_CODE_REGISTER);
                break;
            case 3:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case 4:
                int mode;
                if (MODE_DAY == PrefsUtil.getThemeMode()) {
                    mode = MODE_NIGHT;
                    ((TextView) v).setText(R.string.day);
                } else {
                    mode = MODE_DAY;
                    ((TextView) v).setText(R.string.night);
                }

                Intent themeIntent = new Intent(Constants.ACTION_THEME_CHANGED);
                themeIntent.putExtra(Constants.KEY_THEME_MODE, mode);
                PrefsUtil.setThemeMode(mode);
                sendBroadcast(themeIntent);
                mPagerAdapter.notifyDataSetChanged();
                break;
            case 5:
                if (Utils.isLogin()) {
                    toFavoriteActivity();
                } else {
                    mLoginClickType = LOGIN_FAVORITE_CLICK;
                    new LoginDialog(this, this).show();
                }
                break;
            case 6:
                AboutDialog dialog = new AboutDialog(this, new AboutDialog.AboutDialogListener() {
                    @Override
                    public void onVersionClicked() {
                        easterEgg();
                    }
                });
                dialog.show();
                break;
        }
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    private MediaPlayer mPlayer;

    private void easterEgg() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FrameLayout rootView = (FrameLayout) findViewById(android.R.id.content);
        final FireworksView fireworksView = new FireworksView(this);
        rootView.addView(fireworksView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mPlayer = MediaPlayer.create(this, R.raw.fireworks);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    private long lastBackPressTime;
    private int backPressCount = 0;

    @Override
    public void onBackPressed() {
        if (SpotManager.getInstance(this).disMiss()) {
            return;
        }

        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            if (backPressCount == 0) {
                Utils.showToast(R.string.exit_tips);
                backPressCount += 1;
                lastBackPressTime = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() - lastBackPressTime >= 1000L) {
                    Utils.showToast(R.string.exit_tips);
                    lastBackPressTime = System.currentTimeMillis();
                } else {
                    PrefsUtil.setAdShowed(false);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        SpotManager.getInstance(this).disMiss();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        SpotManager.getInstance(this).unregisterSceenReceiver();
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    protected void applyTheme(int theme) {
        super.applyTheme(theme);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REGISTER
                && resultCode == RESULT_CODE_REGISTER) {
            updateMenuList(true);
        }
    }

    private class FeedPagerAdapter extends FragmentPagerAdapter {
        private int[] mFeedTypeIds = {
                FEED_LIST_HOME,
                FEED_LIST_NOW,
                FEED_LIST_RANDOM
        };

        public FeedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putInt(BUNDLE_KEY_TYPE, mFeedTypeIds[i]);

            return Fragment.instantiate(MainActivity.this,
                    FeedListFragment.class.getName(), bundle);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mFeedTypeIds.length;
        }
    }
}
