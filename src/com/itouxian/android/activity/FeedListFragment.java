package com.itouxian.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.model.FeedData;
import com.itouxian.android.pulltorefresh.PullToRefreshBase;
import com.itouxian.android.pulltorefresh.PullToRefreshListView;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.IntentUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.GifMovieView;
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

/**
 * Created by chenjishi on 14-5-27.
 */
public class FeedListFragment extends Fragment implements Response.Listener<FeedData>,
        Response.ErrorListener, PullToRefreshBase.OnRefreshListener,
        AdapterView.OnItemClickListener, LoginDialog.OnLoginListener {
    public static final String BUNDLE_KEY_TYPE = "type";
    public static final int FEED_LIST_HOME = 1;
    public static final int FEED_LIST_FAVORITE = 2;

    private static final int LOGIN_COMMENT_CLICK = 100;
    private static final int LOGIN_VOTE_CLICK = 101;
    private static final int LOGIN_FAVORITE_CLICK = 102;
    private static final int VOTE_UP = 1;
    private static final int VOTE_DOWN = 2;

    private int mPage = 1;
    private int mType;

    private PullToRefreshListView mPullListView;
    private FeedListAdapter mFeedListAdapter;
    private boolean mIsPulled = false;

    private HashMap<Long, String> mVoteIds = new HashMap<Long, String>();

    private View mFootView;
    private View mEmptyView;

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

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, feed.contents);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "爱偷闲分享"));

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
            mType = bundle.getInt(BUNDLE_KEY_TYPE, FEED_LIST_HOME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPullListView = (PullToRefreshListView) inflater.inflate(R.layout.fragment_feed_list, container, false);
        ListView listView = mPullListView.getRefreshableView();

        mFootView = inflater.inflate(R.layout.load_more, null);
        mEmptyView = inflater.inflate(R.layout.empty_view, null);
        Button loadBtn = (Button) mFootView.findViewById(R.id.btn_load);
        loadBtn.setOnClickListener(mLoadMoreClickListener);

        mFootView.setVisibility(View.GONE);
        listView.addFooterView(mFootView);

        ((ViewGroup) listView.getParent()).addView(mEmptyView);
        listView.setEmptyView(mEmptyView);

        mFeedListAdapter = new FeedListAdapter(getActivity());

        listView.setAdapter(mFeedListAdapter);
        listView.setOnItemClickListener(this);

        mPullListView.setOnRefreshListener(this);

        return mPullListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        mIsPulled = true;
        mPage = 1;
        loadData();
    }

    private void setPullComplete() {
        if (mIsPulled) {
            mIsPulled = false;
            mPullListView.onRefreshComplete();
        }
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
        } else {
            mFootView.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        String url;
        if (mType == FEED_LIST_FAVORITE) {
            String token = PrefsUtil.getUser().token;
            url = String.format("http://www.itouxian.com/json/get_favourite/%1$d?token=%2$s",
                    mPage, token);
        } else {
            url = String.format("http://www.itouxian.com/json/index/%1$d", mPage);
        }

        HttpUtils.get(url, FeedData.class, this, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("feed", mFeedList.get(position - 1));
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

        String url = "http://www.itouxian.com/json/assess/%1$d?token=%2$s&assess=%3$d";
        HttpUtils.get(String.format(url, feedId, token, mVoteType), new Response.Listener<String>() {
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
        private RelativeLayout.LayoutParams mLayoutParams;

        public FeedListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mImageLoader = HttpUtils.getImageLoader();
            mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mDate = new Date(System.currentTimeMillis());

            mContentPattern = Pattern.compile("<p>(.*?)</p>");
            mContentMatcher = mContentPattern.matcher("");

            mPattern = Pattern.compile("src=\"(.*?)\"");
            mMatcher = mPattern.matcher("");

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            float density = metrics.density;
            mImageWidth = width - (int) ((12 * 2 + 8 * 2) * density + .5f);

            mLayoutParams = new RelativeLayout.LayoutParams(mImageWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        public RelativeLayout contentBox;
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
