package com.itouxian.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Comment;
import com.itouxian.android.model.CommentData;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import volley.Response;
import volley.VolleyError;
import volley.toolbox.ImageLoader;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.itouxian.android.util.Constants.MODE_NIGHT;
import static com.itouxian.android.util.Constants.URL_COMMENTS_GET;

/**
 * Created by chenjishi on 14-6-19.
 */
public class CommentListView extends ListView implements Response.Listener<CommentData>,
        Response.ErrorListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private View mFootView;

    private CommentAdapter mAdapter;
    private CommentCallback mCallback;

    private String mToken;

    private long mFeedId;
    private int mPage = 1;

    public CommentListView(Context context) {
        super(context);
        init(context);
    }

    public CommentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommentListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mAdapter = new CommentAdapter(context);

        mFootView = LayoutInflater.from(context).inflate(R.layout.load_more, null);
        mFootView.setVisibility(GONE);
        Button button = (Button) mFootView.findViewById(R.id.btn_load);
        button.setOnClickListener(this);

        Resources res = getResources();

        setCacheColorHint(0x00000000);
        setBackgroundColor(0x00000000);
        setFadingEdgeLength(0);
        setSelector(R.drawable.black_0_transparent);
        setFooterDividersEnabled(false);
        addFooterView(mFootView);
        setAdapter(mAdapter);
        setOnItemClickListener(this);

        if (MODE_NIGHT == PrefsUtil.getThemeMode()) {
            setDivider(res.getDrawable(R.drawable.split_color_night));
            button.setBackgroundResource(R.drawable.btn_gray_night);
            button.setTextColor(res.getColor(R.color.text_color_summary));
        } else {
            setDivider(res.getDrawable(R.drawable.split_color));
            button.setBackgroundResource(R.drawable.btn_gray);
            button.setTextColor(res.getColor(R.color.text_color_regular));
        }
        setDividerHeight(1);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (null != mCallback) {
            mCallback.onCommentError(getContext().getString(R.string.comment_fetch_fail));
        }
        mFootView.setVisibility(View.GONE);
    }

    @Override
    public void onResponse(CommentData response) {
        if (null != response && null != response.data) {
            int count = response.data.data.size();

            if (count > 0) {
                if (count >= 30) {
                    mFootView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    mFootView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
                    mFootView.setVisibility(View.VISIBLE);
                } else {
                    mFootView.setVisibility(View.GONE);
                }

                if (1 == mPage) mAdapter.clearData();

                mAdapter.updateData(response.data.data);
            } else {
                if (null != mCallback) {
                    mCallback.onCommentError(getContext().getString(R.string.no_comment));
                }
                mFootView.setVisibility(View.GONE);
            }
        } else {
            mFootView.setVisibility(GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Comment comment = mAdapter.getItem(position - 1);
        if (null != mCallback) {
            mCallback.onCommentClicked(comment.id, comment.usr.nickname);
        }
    }

    @Override
    public void onClick(View v) {
        mFootView.findViewById(R.id.btn_load).setVisibility(View.GONE);
        mFootView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        mPage++;
        request();
    }

    private void request() {
        String url = String.format(URL_COMMENTS_GET, mFeedId, mPage, mToken);
        HttpUtils.get(url, CommentData.class, this, this);
    }

    public void setOnCommentFetchCallback(CommentCallback callback) {
        mCallback = callback;
    }

    public void requestComment(long id) {
        mFeedId = id;
        UserInfo userInfo = PrefsUtil.getUser();
        if (null != userInfo) {
            mToken = userInfo.token;
            request();
        }
    }

    public void refreshComment(long id) {
        mFeedId = id;
        mPage = 1;
        UserInfo userInfo = PrefsUtil.getUser();
        if (null != userInfo) {
            mToken = userInfo.token;
            request();
        }
    }

    private static class CommentAdapter extends BaseAdapter {
        private List<Comment> mDataList;

        private LayoutInflater mInflater;
        private Format mFormat;
        private Date mDate;
        private ImageLoader mImageLoader;

        private Pattern mPattern;
        private Matcher mMatcher;

        private int mTheme;

        private Resources mRes;

        public CommentAdapter(Context context) {
            mDataList = new ArrayList<Comment>();

            mInflater = LayoutInflater.from(context);
            mFormat = new SimpleDateFormat("yyyy-MM-dd");
            mDate = new Date(System.currentTimeMillis());
            mImageLoader = HttpUtils.getImageLoader();

            mPattern = Pattern.compile("(.*?)<div class='reply-quote'>(.*?)<\\/div>");
            mMatcher = mPattern.matcher("");

            mTheme = PrefsUtil.getThemeMode();

            mRes = context.getResources();
        }

        public void clearData() {
            mDataList.clear();
            notifyDataSetChanged();
        }

        public void updateData(List<Comment> comments) {
            mDataList.addAll(comments);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Comment getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.comment_item, parent, false);

                holder = new ViewHolder();
                holder.avatarImage = (ImageView) convertView.findViewById(R.id.avatar);
                holder.userText = (TextView) convertView.findViewById(R.id.user_name);
                holder.contentText = (TextView) convertView.findViewById(R.id.content);
                holder.replyText = (TextView) convertView.findViewById(R.id.reply);
                holder.replyLayout = (LinearLayout) convertView.findViewById(R.id.reply_layout);
                holder.splitLine = convertView.findViewById(R.id.split_v);

                if (MODE_NIGHT == mTheme) {
                    holder.userText.setTextColor(getColor(R.color.action_bar_color));
                    holder.contentText.setTextColor(getColor(R.color.text_color_weak));
                    holder.replyText.setTextColor(getColor(R.color.text_color_summary));
                    holder.splitLine.setBackgroundColor(getColor(R.color.text_color_summary));
                } else {
                    holder.userText.setTextColor(getColor(R.color.action_bar_color));
                    holder.contentText.setTextColor(getColor(R.color.text_color_regular));
                    holder.replyText.setTextColor(getColor(R.color.text_color_weak));
                    holder.splitLine.setBackgroundColor(0xFFD0E5F2);
                }

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            Comment comment = getItem(position);
            long t = comment.create_time * 1000L;
            UserInfo user = comment.usr;
            mDate.setTime(t);

            mImageLoader.get(user.icon, ImageLoader.getImageListener(holder.avatarImage,
                    R.drawable.user_default, R.drawable.user_default));

            String formattedString = user.nickname + " " + (MODE_NIGHT == mTheme
                    ? "<font color='#666666'>" : "<font color='#999999'>") + mFormat.format(mDate) + "</font>";
            holder.userText.setText(Html.fromHtml(formattedString));

            String content = comment.contents;

            if (content.contains("quote")) {
                mMatcher.reset(content);

                String reply = "";
                while (mMatcher.find()) {
                    content = mMatcher.group(1);
                    reply = mMatcher.group(2);
                }

                if (reply.length() > 0) {
                    holder.replyText.setText(Html.fromHtml(reply));
                    holder.replyLayout.setVisibility(View.VISIBLE);
                }
            } else {
                holder.replyLayout.setVisibility(View.GONE);
            }

            holder.contentText.setText(Html.fromHtml(content));

            return convertView;
        }

        private int getColor(int resId) {
            return Utils.getColor(mRes, resId);
        }
    }

    private static class ViewHolder {
        public ImageView avatarImage;
        public TextView userText;
        public TextView contentText;
        public TextView replyText;
        public LinearLayout replyLayout;
        public View splitLine;
    }
}
