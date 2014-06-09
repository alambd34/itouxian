package com.itouxian.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.util.Constants;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-7-18
 * Time: 下午4:37
 * To change this template use File | Settings | File Templates.
 */
public class PrefsUtil {
    private static Application mContext = App.getInstance();

    private static final String KEY_DRAFT_CONTENT = "draft_content";
    private static final String KEY_DRAFT_TAGS = "draft_tags";
    private static final String KEY_DRAFT_FILE = "draft_file_path";

    private static final long VERSION_CHECK_INTERVAL = 24 * 60 * 60 * 1000;

    private static final String CONFIG_FILE_NAME = "u148_prefs";

    private static final String KEY_NEXT_TOKEN = "next_from";

    public static final String KEY_UPDATE_TIME = "last_update_time";

    public static final String KEY_CHECK_UPDATE_TIME = "last_check_time";

    public static final String KEY_CHECK_VERSION = "check_version";

    public static final String KEY_VIDEO_UPDATE_TIME = "last_update_time";

    private static final String KEY_ACESS_TOKEN = "access_token";
    private static final String KEY_EXPIRE_IN = "expire_in";
    private static final String KEY_CACHE_CLEAR_TIME = "cache_clear_time";
    private static final String KEY_CACHE_UPDATE_TIME = "cache_update_time";

    private static final String KEY_AD_SHOW_TIME = "ad_show_time";

    private static final String KEY_REGISTER_TIME = "register_time";

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_SEX = "user_sex";
    private static final String KEY_USER_ICON = "user_icon";
    private static final String KEY_USER_TOKEN = "user_token";

    private static final String KEY_AD_SHOWED = "key_ad_showed";
    private static final String KEY_THEME_MODE = "theme_mode";

    private static final String KEY_CHANNELS = "channels";

    public static final String KEY_BLESSING = "blessing";
    public static final String DEFAULT_BLESSING = "This Is For YEHAN ZHOU|The Girl I Met Who Brights My Life";

    public static void saveChannels(String s) {
        saveStringPreference(KEY_CHANNELS, s);
    }

    public static String getChannels() {
        return getStringPreference(KEY_CHANNELS);
    }

    public static void saveBlessing(String s) {
        saveStringPreference(KEY_BLESSING, s);
    }

    public static String getBlessing() {
        String s = getStringPreference(KEY_BLESSING);
        if (TextUtils.isEmpty(s)) {
            s = DEFAULT_BLESSING;
        }
        return s;
    }

    public static void saveDraft(String content, String tags, String path) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_DRAFT_CONTENT, content);
        editor.putString(KEY_DRAFT_TAGS, tags);
        editor.putString(KEY_DRAFT_FILE, path);
        editor.commit();
    }

    public static String[] getDraft() {
        String[] drafts = null;
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        String content = preferences.getString(KEY_DRAFT_CONTENT, "");
        String tags = preferences.getString(KEY_DRAFT_TAGS, "");
        String filePath = preferences.getString(KEY_DRAFT_FILE, "");

        if (!TextUtils.isEmpty(content) &&
                !TextUtils.isEmpty(tags) && !TextUtils.isEmpty(filePath)) {
            drafts = new String[3];
            drafts[0] = content;
            drafts[1] = tags;
            drafts[2] = filePath;
        }

        return drafts;
    }

    public static int getThemeMode() {
        return getIntPreference(KEY_THEME_MODE, Constants.MODE_DAY);
    }

    public static void setThemeMode(int mode) {
        saveIntPreference(KEY_THEME_MODE, mode);
    }

    public static void setAdShowed(boolean b) {
        saveBoolPreference(KEY_AD_SHOWED, b);
    }

    public static boolean isAdShowed() {
        return getBoolPreference(KEY_AD_SHOWED);
    }

    public static void setRegisterTime(long time) {
        saveLongPreference(KEY_REGISTER_TIME, time);
    }

    public static long getRegisterTime() {
        return getLongPreferences(KEY_REGISTER_TIME, -1L);
    }

    public static void setUser(UserInfo user) {
        if (null != user && !TextUtils.isEmpty(user.token)) {
            saveStringPreference(KEY_USER_NAME, user.nickname);
            saveStringPreference(KEY_USER_SEX, user.sexStr);
            saveStringPreference(KEY_USER_ICON, user.icon);
            saveStringPreference(KEY_USER_TOKEN, user.token);
        } else {
            saveStringPreference(KEY_USER_NAME, "");
            saveStringPreference(KEY_USER_SEX, "");
            saveStringPreference(KEY_USER_ICON, "");
            saveStringPreference(KEY_USER_TOKEN, "");
        }
    }

    public static UserInfo getUser() {
        String token = getStringPreference(KEY_USER_TOKEN);

        if (!TextUtils.isEmpty(token)) {
            UserInfo user = new UserInfo();

            user.nickname = getStringPreference(KEY_USER_NAME);
            user.sexStr = getStringPreference(KEY_USER_SEX);
            user.icon = getStringPreference(KEY_USER_ICON);
            user.token = getStringPreference(KEY_USER_TOKEN);

            return user;
        } else {
            return null;
        }
    }

    public static void setClearCacheTime(long t) {
        saveLongPreference(KEY_CACHE_CLEAR_TIME, t);
    }

    public static long getClearCacheTime() {
        return getLongPreferences(KEY_CACHE_CLEAR_TIME, -1L);
    }

    public static void saveAccessToken(Oauth2AccessToken token) {
        SharedPreferences.Editor editor = App.getInstance().getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ACESS_TOKEN, token.getToken());
        editor.putLong(KEY_EXPIRE_IN, token.getExpiresTime());
        editor.commit();
    }

    public static Oauth2AccessToken getAccessToken() {
        Context context = App.getInstance();
        Oauth2AccessToken token = new Oauth2AccessToken();
        SharedPreferences prfs = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        token.setToken(prfs.getString(KEY_ACESS_TOKEN, ""));
        token.setExpiresTime(prfs.getLong(KEY_EXPIRE_IN, 0L));
        return token;
    }

    private static int getIntPreference(String key, int defaultVal) {
        return mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getInt(key, defaultVal);
    }

    private static void saveIntPreference(String key, int value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static long getLongPreferences(String key, long defaultVal) {
        return mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getLong(key, defaultVal);
    }

    public static void saveLongPreference(String key, long value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static boolean getBoolPreference(String key) {
        return mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    private static void saveBoolPreference(String key, boolean value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static String getStringPreference(String key) {
        return mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getString(key, "");
    }

    private static void saveStringPreference(String key, String value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }
}
