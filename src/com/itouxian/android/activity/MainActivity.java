package com.itouxian.android.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.itouxian.android.util.IntentUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.AboutDialog;
import com.itouxian.android.view.FireworksView;
import com.itouxian.android.view.LoginDialog;
import volley.toolbox.ImageLoader;

import static com.itouxian.android.util.ConstantUtil.MODE_DAY;
import static com.itouxian.android.util.ConstantUtil.MODE_NIGHT;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        DrawerLayout.DrawerListener, LoginDialog.OnLoginListener {
    private static final int LOGIN_FAVORITE_CLICK = 100;
    private static final int LOGIN_EDIT_CLICK = 101;
    public static final int REQUEST_CODE_REGISTER = 101;
    public static final int RESULT_CODE_REGISTER = 102;

    private int mLoginClickType;

    private TextView mTitleText;
    private DrawerLayout mDrawerLayout;

    private ImageLoader mImageLoader;

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

        Bundle bundle = new Bundle();
        bundle.putInt(FeedListFragment.BUNDLE_KEY_TYPE, FeedListFragment.FEED_LIST_HOME);
        FeedListFragment fragment = (FeedListFragment) Fragment.instantiate(this,
                FeedListFragment.class.getName(), bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.commit();
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

        ImageView userIcon = new ImageView(this);
        userIcon.setLayoutParams(new LinearLayout.LayoutParams(dp2px(20), dp2px(20)));
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
            mMenuContainer.addView(generateView(R.string.register, R.drawable.user_default, index), lp);
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
    }

    @Override
    public void onLoginError() {

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
                    Toast.makeText(this, "已登陆", Toast.LENGTH_SHORT).show();
                } else {
                    new LoginDialog(this, new LoginDialog.OnLoginListener() {
                        @Override
                        public void onLoginSuccess() {
                            updateMenuList(true);
                        }

                        @Override
                        public void onLoginError() {

                        }
                    }).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REGISTER
                && resultCode == RESULT_CODE_REGISTER) {
            updateMenuList(true);
        }
    }
}
