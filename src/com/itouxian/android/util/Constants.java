package com.itouxian.android.util;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午6:59
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final String FLURRY_REGISTER_SUCCESS = "REGISTER_SUCCESS";
    public static final String FLURRY_REGISTER_FAIL = "REGISTER_FAIL";
    public static final String FLURRY_REGISTER_BUTTON = "REGISTER_BUTTON_SUBMIT";
    public static final String FLURRY_POST_BUTTON = "POST_BUTTON_SUBMIT";
    public static final String FLURRY_POST_SUCCESS = "POST_SUCCESS";
    public static final String FLURRY_POST_FAIL = "POST_FAIL";
    public static final String FLURRY_CHANNEL = "CHANNEL";

    public static final String HOST_PATH = "http://www.itouxian.com/json/";

    public static final String URL_FAVORITE = HOST_PATH + "favourite";
    public static final String URL_FAVORITE_GET = HOST_PATH + "get_favourite/%1$d?token=%2$s";
    public static final String URL_FEED_RANDOM = HOST_PATH + "random";
    public static final String URL_FEED_NOW = HOST_PATH + "now/%1$d";
    public static final String URL_FEED_HOME = HOST_PATH + "index/%1$d";
    public static final String URL_VOTE = HOST_PATH + "assess/%1$d?token=%2$s&assess=%3$d";
    public static final String URL_COMMENTS_GET = HOST_PATH + "get_comment/%1$d/%2$d?token=%3$s";
    public static final String URL_COMMENTS = HOST_PATH + "comment";
    public static final String URL_REGISTER = HOST_PATH + "register";
    public static final String URL_LOGIN = HOST_PATH + "login";
    public static final String URL_UPLOAD = HOST_PATH + "upload";
    public static final String URL_UPDATE = HOST_PATH + "version";

    public static final String BUNDLE_KEY_FEED = "feed";
    public static final String KEY_FEED_LIST = "feed_list";
    public static final String KEY_FEED_INDEX = "index";

    public static final String ACTION_THEME_CHANGED = "com.itouxian.android.THEME_CHANGE";
    public static final String KEY_THEME_MODE = "theme_mode";

    public static final int MODE_DAY = 0;
    public static final int MODE_NIGHT = 1;

    public static int FEED_UNKNOWN = 0;
    public static int FEED_TEXT = 1;
    public static int FEED_SINGLE_IMAGE = 2;
    public static int FEED_IMAGE_GIF = 3;
    public static int FEED_VIDEO = 4;
    public static int FEED_MUSIC = 5;

    public static final String WX_APP_ID = "wxf862baa09e0df157";
    public static final String WEIBO_APP_KEY = "1792649719";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";
}
