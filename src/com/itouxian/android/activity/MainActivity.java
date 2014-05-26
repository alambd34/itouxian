package com.itouxian.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.model.FeedData;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.pulltorefresh.PullToRefreshBase;
import com.itouxian.android.pulltorefresh.PullToRefreshListView;
import com.itouxian.android.util.Constants;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.IntentUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.GifMovieView;
import com.itouxian.android.view.LoginDialog;
import org.w3c.dom.Text;
import volley.Response;
import volley.VolleyError;
import volley.toolbox.ImageLoader;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.itouxian.android.util.ConstantUtil.*;

public class MainActivity extends BaseActivity implements Response.Listener<FeedData>, AdapterView.OnItemClickListener,
        Response.ErrorListener, PullToRefreshBase.OnRefreshListener, View.OnClickListener, DrawerLayout.DrawerListener {
    public static final String REQUEST_URL = "http://www.itouxian.com/json/index/%1$d";
    private int mCurrentPage = 1;

    private PullToRefreshListView mPullToRefresh;

    private FeedAdapter mFeedAdapter;
    private boolean mIsPulled = false;
    public ArrayList<Feed> mFeedList = new ArrayList<Feed>();

    private View mFootView;
    private View mEmptyView;
    private TextView mTitleText;
    private DrawerLayout mDrawerLayout;

    private ImageLoader mImageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main, R.layout.home_title_layout);
        setRightButtonIcon(R.drawable.ic_edit);

        mPullToRefresh = (PullToRefreshListView) findViewById(R.id.list_feed);
        ListView listView = mPullToRefresh.getRefreshableView();

        mTitleText = (TextView) findViewById(R.id.title_text);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFootView = inflater.inflate(R.layout.load_more, null);
        mEmptyView = inflater.inflate(R.layout.empty_view, null);
        Button loadBtn = (Button) mFootView.findViewById(R.id.btn_load);
        loadBtn.setOnClickListener(this);

        mFootView.setVisibility(View.GONE);
        listView.addFooterView(mFootView);

        ((ViewGroup) listView.getParent()).addView(mEmptyView);
        listView.setEmptyView(mEmptyView);

        mFeedAdapter = new FeedAdapter(this);

        listView.setAdapter(mFeedAdapter);
        listView.setOnItemClickListener(this);

        mPullToRefresh.setOnRefreshListener(this);
        mImageLoader = HttpUtils.getImageLoader();

        updateMenuList(Utils.isLogin());
        loadData();
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

    private void loadData() {
        final String url = String.format(REQUEST_URL, mCurrentPage);

        HttpUtils.get(url, FeedData.class, this, this);
    }

    @Override
    public void onRightButtonClicked(View view) {
        if (!Utils.isLogin()) {
            new LoginDialog(this, new LoginDialog.OnLoginListener() {
                @Override
                public void onLoginSuccess() {
                    toPostActivity();
                }

                @Override
                public void onLoginError() {

                }
            }).show();
        } else {
            toPostActivity();
        }
    }

    private void toPostActivity() {
        Intent intent = new Intent(this, FeedPostActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        mIsPulled = true;
        mCurrentPage = 1;
        loadData();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setPullComplete();
        mFootView.setVisibility(View.GONE);
    }

    @Override
    public void onResponse(FeedData response) {
        setPullComplete();

        if (null != response && null != response.data) {
            final ArrayList<Feed> feeds = response.data.data;

            if (null != feeds && feeds.size() > 0) {
                if (1 == mCurrentPage) {
                    mFeedList.clear();
                }
                mFeedList.addAll(feeds);
                mFeedAdapter.notifyDataSetChanged();

                mFootView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                mFootView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
                mFootView.setVisibility(View.VISIBLE);
            } else {
                mFootView.setVisibility(View.GONE);
            }
        } else {
            mFootView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_load) {
            mFootView.findViewById(R.id.btn_load).setVisibility(View.GONE);
            mFootView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
            mCurrentPage++;
            loadData();
        } else {
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
                    break;
                case 2:
                    break;
                case 3:
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
                    break;
                case 6:
                    break;
            }

        }
    }

    void setPullComplete() {
        if (mIsPulled) {
            mIsPulled = false;
            mPullToRefresh.onRefreshComplete();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("feed", mFeedList.get(position - 1));
        IntentUtils.startPreviewActivity(this, intent);
    }

    private class FeedAdapter extends BaseAdapter {
        private static final int MAX_TYPE_COUNT = 6;

        private Context mContext;

        private LayoutInflater mInflater;
        private Format mFormat;
        private Date mDate;

        private Pattern mPattern;
        private Matcher mMatcher;

        private Pattern mContentPattern;
        private Matcher mContentMatcher;
        private int mImageWidth;
        private RelativeLayout.LayoutParams mLayoutParams;

        public FeedAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mDate = new Date(System.currentTimeMillis());

            mContentPattern = Pattern.compile("<p>(.*?)</p>");
            mContentMatcher = mContentPattern.matcher("");

            mPattern = Pattern.compile("src=\"(.*?)\"");
            mMatcher = mPattern.matcher("");

            final int screenWidth = getResources().getDisplayMetrics().widthPixels;
            mImageWidth = screenWidth - (int) ((12 * 2 + 8 * 2) * mDensity + .5f);

            mLayoutParams = new RelativeLayout.LayoutParams(mImageWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        @Override
        public int getViewTypeCount() {
            return MAX_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            final Feed feed = getItem(position);
            int feedType = feed.feed_type;

            if (FEED_UNKNOWN != feedType) {
                return feedType;
            } else {
                final String content = feed.contents;
                mMatcher.reset(content);

                String imageUrl = "";
                while (mMatcher.find()) {
                    imageUrl = mMatcher.group(1);
                }

                if (!TextUtils.isEmpty(imageUrl)) {
                    if (imageUrl.endsWith(".gif") || imageUrl.endsWith(".GIF")) {
                        feedType = FEED_IMAGE_GIF;
                    } else {
                        feedType = FEED_SINGLE_IMAGE;
                    }

                    feed.imageUrl = imageUrl;
                    feed.feed_type = feedType;
                    return feedType;
                } else {
                    feedType = FEED_TEXT;
                    feed.feed_type = feedType;
                    return feedType;
                }
            }
        }

        @Override
        public int getCount() {
            return mFeedList.size();
        }

        @Override
        public Feed getItem(int position) {
            return mFeedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.list_item_feed, parent, false);
                holder = new ViewHolder();

                holder.contentBox = (RelativeLayout) convertView.findViewById(R.id.content_box);
                holder.avatarView = (ImageView) convertView.findViewById(R.id.avatar);
                holder.shareButton = (ImageButton) convertView.findViewById(R.id.share_content);
                holder.favoriteButton = (ImageButton) convertView.findViewById(R.id.favorite);
                holder.nameText = (TextView) convertView.findViewById(R.id.user_name);
                holder.timeText = (TextView) convertView.findViewById(R.id.pub_time);
                holder.titleText = (TextView) convertView.findViewById(R.id.feed_title);
                holder.contentText = (TextView) convertView.findViewById(R.id.content);
                holder.upText = (TextView) convertView.findViewById(R.id.up_count);
                holder.downText = (TextView) convertView.findViewById(R.id.down_count);
                holder.commentText = (TextView) convertView.findViewById(R.id.comment_count);

                final int itemType = getItemViewType(position);
                if (itemType == FEED_SINGLE_IMAGE) {
                    holder.imageView = new ImageView(mContext);
                    holder.imageView.setId(R.id.feed_image);
                    holder.contentBox.addView(holder.imageView);
                }

                if (itemType == FEED_IMAGE_GIF) {
                    holder.gifMovieView = new GifMovieView(mContext);
                    holder.gifMovieView.setId(R.id.feed_image);
                    holder.contentBox.addView(holder.gifMovieView);
                }

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            final Feed feed = getItem(position);

            mImageLoader.get(feed.usr.icon, ImageLoader.getImageListener(holder.avatarView,
                    R.drawable.icon, R.drawable.icon));
            holder.nameText.setText(feed.usr.nickname);

            final long t = feed.create_time * 1000L;
            mDate.setTime(t);

            holder.timeText.setText("发布于 " + mFormat.format(mDate));

            final String title = feed.title;
            if (!TextUtils.isEmpty(title)) {
                holder.titleText.setText(title);
                holder.titleText.setVisibility(View.VISIBLE);
            } else {
                holder.titleText.setVisibility(View.GONE);
            }

            holder.commentText.setText(String.valueOf(feed.count_review));
            holder.upText.setText(String.valueOf(feed.count_support));
            holder.downText.setText(String.valueOf(feed.count_tread));

            final int itemType = getItemViewType(position);
            if (itemType == FEED_SINGLE_IMAGE || itemType == FEED_IMAGE_GIF) {
                StringBuilder sb = new StringBuilder();
                mContentMatcher.reset(feed.contents);
                while (mContentMatcher.find()) {
                    final String content = mContentMatcher.group(1);
                    if (!content.contains("src")) {
                        sb.append(content);
                    }
                }

                String contents = sb.toString();

                if (!TextUtils.isEmpty(contents)) {
                    if (contents.length() >= 140) {
                        contents = contents.substring(0, 139);
                    }
                    holder.contentText.setText(Html.fromHtml(contents));
                    holder.contentText.setVisibility(View.VISIBLE);
                } else {
                    holder.contentText.setVisibility(View.GONE);
                }

                if (itemType == FEED_IMAGE_GIF) {
                    holder.gifMovieView.setImageUrl(feed.imageUrl, mImageWidth);
                } else {
                    final ImageView feedImageView = holder.imageView;
                    mImageLoader.get(feed.imageUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            Bitmap bitmap = response.getBitmap();
                            if (null != bitmap) {
                                int w = bitmap.getWidth();
                                int h = bitmap.getHeight();

                                Bitmap cropedBitmap = null;
                                if (h >= 1000) {
                                    h = 650;
                                    cropedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h);
                                }

                                int requestHeight = mImageWidth * h / w;
                                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mImageWidth, requestHeight);
                                feedImageView.setLayoutParams(lp);

                                if (null != cropedBitmap) {
                                    feedImageView.setImageBitmap(cropedBitmap);
                                } else {
                                    feedImageView.setImageBitmap(bitmap);
                                }
                            } else {
                                feedImageView.setImageResource(R.drawable.icon);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            feedImageView.setImageResource(R.drawable.icon);
                        }
                    });
                }
            } else {
                String content = feed.contents;
                if (!TextUtils.isEmpty(content)) {
                    if (content.length() >= 140) {
                        content = content.substring(0, 139);
                    }
                }
                holder.contentText.setText(Html.fromHtml(content));
            }

            holder.commentText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final long feedId = feed.id;
                    if (Utils.isLogin()) {
                        startCommentActivity(feedId);
                    } else {
                        new LoginDialog(MainActivity.this, new LoginDialog.OnLoginListener() {
                            @Override
                            public void onLoginSuccess() {
                                startCommentActivity(feedId);
                            }

                            @Override
                            public void onLoginError() {

                            }
                        });
                    }
                }
            });

            return convertView;
        }
    }

    private void startCommentActivity(long id) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra(IntentUtils.KEY_FEED_ID, id);
        startActivity(intent);
    }

    private class ViewHolder {
        private RelativeLayout contentBox;
        private ImageView avatarView;
        private ImageButton shareButton;
        private ImageButton favoriteButton;
        private TextView nameText;
        private TextView timeText;
        private TextView titleText;
        private TextView contentText;
        private ImageView imageView;
        private GifMovieView gifMovieView;
        private TextView upText;
        private TextView downText;
        private TextView commentText;
    }
}
