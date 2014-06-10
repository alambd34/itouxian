package com.itouxian.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.*;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.model.FeedData;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.IntentUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.GifMovieView;
import com.itouxian.android.view.LoadingView;
import com.itouxian.android.view.LoginDialog;
import org.json.JSONException;
import org.json.JSONObject;
import volley.Response;
import volley.VolleyError;
import volley.toolbox.ImageLoader;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.itouxian.android.util.ConstantUtil.*;
import static com.itouxian.android.util.Constants.*;
import static com.itouxian.android.util.Constants.MODE_NIGHT;

/**
 * Created by chenjishi on 14-5-27.
 */
public class FeedListFragment extends Fragment implements Response.Listener<FeedData>, SwipeRefreshLayout.OnRefreshListener,
        Response.ErrorListener, AdapterView.OnItemClickListener, LoginDialog.OnLoginListener {
    public static final String BUNDLE_KEY_TYPE = "feed_list_type";

    public static final int FEED_LIST_HOME = 1;
    public static final int FEED_LIST_NOW = 2;
    public static final int FEED_LIST_RANDOM = 3;
    public static final int FEED_LIST_FAVORITE = 4;

    private static final int LOGIN_COMMENT_CLICK = 100;
    private static final int LOGIN_VOTE_CLICK = 101;
    private static final int LOGIN_FAVORITE_CLICK = 102;
    private static final int VOTE_UP = 1;
    private static final int VOTE_DOWN = 2;

    private int mPage = 1;
    private int mFeedListType;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FeedListAdapter mFeedListAdapter;

    private HashMap<Long, String> mVoteIds = new HashMap<Long, String>();

    private FrameLayout mContainer;
    private LoadingView mLoadingView;
    private View mFootView;

    private int mLoginClickType;
    private long mClickedItemId;
    private int mVoteType;

    private ArrayList<Feed> mFeedList = new ArrayList<Feed>();

    private View.OnClickListener mCommentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long feedId = (Long) v.getTag();
            if (Utils.isLogin()) {
                startCommentActivity(feedId);
            } else {
                mLoginClickType = LOGIN_COMMENT_CLICK;
                mClickedItemId = feedId;
                new LoginDialog(getActivity(), FeedListFragment.this).show();
            }
        }
    };

    private View.OnClickListener mVoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long feedId = (Long) v.getTag();
            mVoteType = R.id.up_count == v.getId() ? VOTE_UP : VOTE_DOWN;

            if (Utils.isLogin()) {
                /** voted already */
                if (!TextUtils.isEmpty(mVoteIds.get(feedId))) {
                    return;
                }
                vote(feedId);

                TextView view = (TextView) v;

                int count = Integer.parseInt(view.getText().toString());
                count += 1;
                view.setText(String.valueOf(count));

                mVoteIds.put(feedId, "hit");
            } else {
                mLoginClickType = LOGIN_VOTE_CLICK;
                mClickedItemId = feedId;
                new LoginDialog(getActivity(), FeedListFragment.this).show();
            }
        }
    };

    private View.OnClickListener mShareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            Feed feed = mFeedList.get(position);

            String imageUrl = feed.imageUrl;
            if (!TextUtils.isEmpty(imageUrl)) {
                Utils.shareImage(getActivity(), imageUrl);
            } else {
                String content = feed.contents;
                content = content.replaceAll("<p>|<\\/p>|&(.*?);", "");
                content += " - 来自爱偷闲 iTouxian.Com";

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_from)));
            }
        }
    };

    private View.OnClickListener mLoadMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFootView.findViewById(R.id.btn_load).setVisibility(View.GONE);
            mFootView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
            mPage++;
            loadData();
        }
    };

    private View.OnClickListener mFavoriteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long feedId = (Long) v.getTag();
            if (Utils.isLogin()) {
                favorite(feedId);
            } else {
                mLoginClickType = LOGIN_FAVORITE_CLICK;
                mClickedItemId = feedId;
                new LoginDialog(getActivity(), FeedListFragment.this).show();
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            mFeedListType = bundle.getInt(BUNDLE_KEY_TYPE, FEED_LIST_HOME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed_list, container, false);

        mContainer = (FrameLayout) view.findViewById(R.id.container);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        ListView listView = (ListView) view.findViewById(R.id.list_feed);

        int theme = PrefsUtil.getThemeMode();
        mFootView = inflater.inflate(R.layout.load_more, null);
        Button loadBtn = (Button) mFootView.findViewById(R.id.btn_load);
        loadBtn.setOnClickListener(mLoadMoreClickListener);
        loadBtn.setBackgroundResource(MODE_NIGHT == theme ?
        R.drawable.card_bkg_night : R.drawable.card_bkg);
        loadBtn.setTextColor(MODE_NIGHT == theme ? 0xFF999999 : 0xFF333333);

        mFootView.setVisibility(View.GONE);
        listView.addFooterView(mFootView);

        mFeedListAdapter = new FeedListAdapter(getActivity());

        listView.setAdapter(mFeedListAdapter);
        listView.setOnItemClickListener(this);

        mSwipeRefreshLayout.setColorScheme(R.color.color1,
                R.color.color2, R.color.color3, R.color.color4);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSwipeRefreshLayout.setRefreshing(true);
        showLoadingView();
        loadData();
    }

    private void showLoadingView() {
        if (null == mLoadingView) {
            mLoadingView = new LoadingView(getActivity());
        }

        ViewParent parent = mLoadingView.getParent();
        if (null != parent) ((FrameLayout) parent).removeView(mLoadingView);

        mContainer.addView(mLoadingView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void hideLoadingView() {
        mContainer.removeView(mLoadingView);
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        loadData();
    }

    private void setPullComplete() {
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 200);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setPullComplete();
        mFootView.setVisibility(View.GONE);

        int resId;
        if (!Utils.didNetworkConnected(getActivity())) {
            resId = R.string.net_error;
        } else {
            resId = R.string.server_error;
        }
        mLoadingView.setErrorTips(getString(resId));
    }

    @Override
    public void onResponse(FeedData response) {
        setPullComplete();

        if (null != response && null != response.data) {
            final ArrayList<Feed> feeds = response.data.data;

            if (null != feeds && feeds.size() > 0) {
                if (1 == mPage) {
                    mFeedList.clear();
                }
                mFeedList.addAll(feeds);
                mFeedListAdapter.notifyDataSetChanged();

                mFootView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                mFootView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
                mFootView.setVisibility(View.VISIBLE);
            } else {
                mFootView.setVisibility(View.GONE);
            }
            hideLoadingView();
        } else {
            mFootView.setVisibility(View.GONE);
            mLoadingView.setErrorTips(getString(R.string.server_error));
        }
    }

    private void loadData() {
        String url = null;
        switch (mFeedListType) {
            case FEED_LIST_HOME:
                url = String.format(URL_FEED_HOME, mPage);
                break;
            case FEED_LIST_NOW:
                url = String.format(URL_FEED_NOW, mPage);
                break;
            case FEED_LIST_RANDOM:
                url = URL_FEED_RANDOM;
                break;
            case FEED_LIST_FAVORITE:
                String token = PrefsUtil.getUser().token;
                url = String.format(URL_FAVORITE_GET, mPage, token);
                break;
        }

        HttpUtils.get(url, FeedData.class, this, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(KEY_FEED_LIST, mFeedList);
        intent.putExtra(KEY_FEED_INDEX, position);
        startActivity(intent);
    }

    @Override
    public void onLoginSuccess() {
        switch (mLoginClickType) {
            case LOGIN_COMMENT_CLICK:
                startCommentActivity(mClickedItemId);
                break;
            case LOGIN_VOTE_CLICK:
                vote(mClickedItemId);
                break;
            case LOGIN_FAVORITE_CLICK:
                favorite(mClickedItemId);
                break;
        }
    }

    @Override
    public void onLoginError() {

    }

    private void vote(long feedId) {
        String token = PrefsUtil.getUser().token;

        HttpUtils.get(String.format(URL_VOTE, feedId, token, mVoteType), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    private void favorite(long feedId) {
        String token = PrefsUtil.getUser().token;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(feedId));
        params.put("token", token);

        HttpUtils.post(URL_FAVORITE, params, new Response.Listener<String>() {
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

    private void startCommentActivity(long id) {
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra(IntentUtils.KEY_FEED_ID, id);
        startActivity(intent);
    }

    private class FeedListAdapter extends BaseAdapter {
        private static final int MAX_TYPE_COUNT = 6;

        private Context mContext;
        private ImageLoader mImageLoader;

        private LayoutInflater mInflater;
        private Format mFormat;
        private Date mDate;

        private Pattern mPattern;
        private Matcher mMatcher;

        private Pattern mContentPattern;
        private Matcher mContentMatcher;
        private int mImageWidth;

        private Resources mRes;
        private int mPadding;

        public FeedListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mImageLoader = HttpUtils.getImageLoader();
            mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mDate = new Date(System.currentTimeMillis());

            mRes = getResources();
            mPadding = (int) (mRes.getDisplayMetrics().density * 8);

            mContentPattern = Pattern.compile("<p>(.*?)</p>");
            mContentMatcher = mContentPattern.matcher("");

            mPattern = Pattern.compile("src=\"(.*?)\"");
            mMatcher = mPattern.matcher("");

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            float density = metrics.density;
            mImageWidth = width - (int) ((12 * 2 + 8 * 2) * density + .5f);
        }

        @Override
        public int getViewTypeCount() {
            return MAX_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            Feed feed = getItem(position);
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

            final int theme = PrefsUtil.getThemeMode();
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.list_item_feed, parent, false);
                holder = new ViewHolder();

                holder.mContainer = (RelativeLayout) convertView.findViewById(R.id.item_container);
                holder.contentBox = (RelativeLayout) convertView.findViewById(R.id.content_box);
                holder.mDivider = convertView.findViewById(R.id.split_h);
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

                if (MODE_NIGHT == theme) {
                    holder.nameText.setTextColor(mRes.getColor(R.color.text_color_summary));
                    holder.timeText.setTextColor(mRes.getColor(R.color.text_color_summary));
                    holder.titleText.setTextColor(mRes.getColor(R.color.text_color_weak));
                    holder.contentText.setTextColor(mRes.getColor(R.color.text_color_weak));
                    holder.upText.setTextColor(mRes.getColor(R.color.text_color_summary));
                    holder.downText.setTextColor(mRes.getColor(R.color.text_color_summary));
                    holder.commentText.setTextColor(mRes.getColor(R.color.text_color_summary));
                    holder.mDivider.setBackgroundColor(0xFF444444);
                } else {
                    holder.nameText.setTextColor(mRes.getColor(R.color.text_color_weak));
                    holder.timeText.setTextColor(mRes.getColor(R.color.text_color_weak));
                    holder.titleText.setTextColor(mRes.getColor(R.color.text_color_regular));
                    holder.contentText.setTextColor(mRes.getColor(R.color.text_color_regular));
                    holder.upText.setTextColor(mRes.getColor(R.color.text_color_weak));
                    holder.downText.setTextColor(mRes.getColor(R.color.text_color_weak));
                    holder.commentText.setTextColor(mRes.getColor(R.color.text_color_weak));
                    holder.mDivider.setBackgroundColor(0xFFE6E6E6);
                }

                holder.contentBox.setPadding(0, 0, 0, 0);
                final int itemType = getItemViewType(position);
                if (itemType == FEED_SINGLE_IMAGE) {
                    holder.imageView = new ImageView(mContext);
                    holder.imageView.setId(R.id.feed_image);
                    holder.contentBox.addView(holder.imageView);
                    holder.contentBox.setPadding(0, mPadding, 0, 0);
                }

                if (itemType == FEED_IMAGE_GIF) {
                    holder.gifMovieView = new GifMovieView(mContext);
                    holder.gifMovieView.setId(R.id.feed_image);
                    holder.contentBox.addView(holder.gifMovieView);
                    holder.contentBox.setPadding(0, mPadding, 0, 0);
                }

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            holder.mContainer.setBackgroundResource(theme == MODE_NIGHT ? R.drawable.card_bkg_night :
                    R.drawable.card_bkg);
            holder.mContainer.setPadding(mPadding, mPadding, mPadding, mPadding);

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
                holder.titleText.getPaint().setFakeBoldText(true);
                holder.titleText.setVisibility(View.VISIBLE);
            } else {
                holder.titleText.setVisibility(View.GONE);
            }

            holder.commentText.setText(String.valueOf(feed.count_review));
            holder.commentText.setTag(feed.id);
            holder.commentText.setOnClickListener(mCommentClickListener);

            holder.upText.setText(String.valueOf(feed.count_support));
            holder.upText.setTag(feed.id);
            holder.downText.setText(String.valueOf(feed.count_tread));
            holder.downText.setTag(feed.id);

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
                    if (contents.length() >= 300) {
                        contents = contents.substring(0, 299);
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
                    feedImageView.setImageResource(android.R.color.transparent);
                    mImageLoader.get(feed.imageUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            Bitmap bitmap = response.getBitmap();
                            if (null != bitmap) {
                                int w = bitmap.getWidth();
                                int h = bitmap.getHeight();

                                int desiredHeight = mImageWidth * h / w;
                                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mImageWidth, desiredHeight);
                                feedImageView.setLayoutParams(lp);
                                feedImageView.setImageBitmap(bitmap);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }, mImageWidth, 0);
                }
            } else {
                String content = feed.contents;
                if (!TextUtils.isEmpty(content)) {
                    if (content.length() >= 300) {
                        content = content.substring(0, 299);
                    }
                }
                Spanned spanned = Html.fromHtml(content);
                /** content ended with two /n/n(ascii code is 10), so we need remove the end break line */
                CharSequence charSequence = Utils.trim(spanned, 0, spanned.length());

                holder.contentText.setText(charSequence);
            }

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(mShareClickListener);

            holder.favoriteButton.setTag(feed.id);
            holder.favoriteButton.setOnClickListener(mFavoriteClickListener);

            holder.upText.setOnClickListener(mVoteClickListener);
            holder.downText.setOnClickListener(mVoteClickListener);

            return convertView;
        }
    }

    private static class ViewHolder {
        public RelativeLayout mContainer;
        public RelativeLayout contentBox;
        public View mDivider;
        public ImageView avatarView;
        public ImageButton shareButton;
        public ImageButton favoriteButton;
        public TextView nameText;
        public TextView timeText;
        public TextView titleText;
        public TextView contentText;
        public ImageView imageView;
        public GifMovieView gifMovieView;
        public TextView upText;
        public TextView downText;
        public TextView commentText;
    }
}
