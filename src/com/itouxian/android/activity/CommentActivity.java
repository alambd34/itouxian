package com.itouxian.android.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Comment;
import com.itouxian.android.model.CommentData;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.util.Constants;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.itouxian.android.util.IntentUtils.KEY_FEED_ID;

/**
 * Created by chenjishi on 14-4-2.
 */
public class CommentActivity extends BaseActivity implements Response.Listener<CommentData>, Response.ErrorListener,
        View.OnClickListener, AdapterView.OnItemClickListener {
    private long mFeedId;
    private long mReplyId;
    private int mPage = 1;

    private ArrayList<Comment> mCommentList = new ArrayList<Comment>();

    private CommentAdapter mAdapter;

    private View mEmptyView;
    private View mFootView;
    private EditText mEditText;

    private String mToken;
    private String mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        setTitle(R.string.comment);

        mFeedId = getIntent().getExtras().getLong(KEY_FEED_ID);

        UserInfo userInfo = PrefsUtil.getUser();
        mToken = userInfo.token;

        mEmptyView = findViewById(R.id.empty_view);
        mFootView = LayoutInflater.from(this).inflate(R.layout.load_more, null);
        Button button = (Button) mFootView.findViewById(R.id.btn_load);
        button.setOnClickListener(this);

        mEditText = (EditText) findViewById(R.id.et_content);

        mAdapter = new CommentAdapter(this);
        ListView listView = (ListView) findViewById(R.id.list_comment);
        listView.addFooterView(mFootView);
        mFootView.setVisibility(View.GONE);
        listView.setEmptyView(mEmptyView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            button.setBackgroundResource(R.drawable.btn_gray_night);
            button.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color));
            button.setBackgroundResource(R.drawable.btn_gray);
            button.setTextColor(getResources().getColor(R.color.text_color_regular));
        }
        listView.setDividerHeight(1);

        loadData();
    }

    private void loadData() {
        final String url = String.format("http://www.itouxian.com/json/get_comment/%1$d/%2$d?token=%3$s",
                mFeedId, mPage, mToken);
        Log.i("test", "url " + url);
        HttpUtils.get(url, CommentData.class, this, this);
    }

    @Override
    public void onClick(View v) {
        mFootView.findViewById(R.id.btn_load).setVisibility(View.GONE);
        mFootView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        mPage++;
        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Comment comment = mCommentList.get(position);
        mEditText.setHint(getString(R.string.reply, comment.usr.nickname));
        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        mReplyId = comment.id;
    }

    public void onSendButtonClicked(View v) {
        String content = mEditText.getText().toString();

        if (!TextUtils.isEmpty(content)) {
            sendComment(content);
            mReplyId = 0;
        } else {
            Utils.showToast(R.string.comment_empty);
        }
    }

    private void sendComment(final String content) {
        if (content.equals(mContent)) return;

        final String url = "http://www.itouxian.com/json/comment";
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.comment_submitting));
        pd.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(mFeedId));
        params.put("token", mToken);
        params.put("content", getString(R.string.comment_from, content));
        params.put("review_id", String.valueOf(mReplyId));

        HttpUtils.post(url, params, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.dismiss();
                        if (!TextUtils.isEmpty(response)) {
                            try {
                                JSONObject jObj = new JSONObject(response);
                                int code = jObj.optInt("code", -1);

                                if (0 == code) {
                                    mEditText.setText("");
                                    mEditText.setHint("");
                                    mEditText.clearFocus();
                                    mContent = content;
                                    Utils.showToast(R.string.comment_success);
                                    loadData();
                                } else {
                                    Utils.showToast(R.string.comment_fail);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Utils.showToast(R.string.comment_fail);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Utils.showToast(R.string.comment_fail);
                    }
                }
        );
    }

    @Override
    public void onResponse(CommentData response) {
        if (null != response && null != response.data) {
            final int count = response.data.data.size();

            if (count > 0) {
                if (1 == mPage) mCommentList.clear();

                mCommentList.addAll(response.data.data);

                if (count >= 30) {
                    mFootView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    mFootView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
                    mFootView.setVisibility(View.VISIBLE);
                } else {
                    mFootView.setVisibility(View.GONE);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                Utils.setErrorView(mEmptyView, R.string.no_comment);
                mFootView.setVisibility(View.GONE);
            }
        } else {
            mFootView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Utils.setErrorView(mEmptyView, R.string.net_error);
        mFootView.setVisibility(View.GONE);
    }

    private class CommentAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Format mFormat;
        private Date mDate;
        private ImageLoader mImageLoader;

        public CommentAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mFormat = new SimpleDateFormat("yyyy-MM-dd");
            mDate = new Date(System.currentTimeMillis());
            mImageLoader = HttpUtils.getImageLoader();
        }

        @Override
        public int getCount() {
            return mCommentList.size();
        }

        @Override
        public Comment getItem(int position) {
            return mCommentList.get(position);
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

                if (Constants.MODE_NIGHT == mTheme) {
                    holder.userText.setTextColor(getResources().getColor(R.color.action_bar_color));
                    holder.contentText.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.replyText.setTextColor(getResources().getColor(R.color.text_color_summary));
                    holder.splitLine.setBackgroundColor(getResources().getColor(R.color.text_color_summary));
                } else {
                    holder.userText.setTextColor(getResources().getColor(R.color.action_bar_color));
                    holder.contentText.setTextColor(getResources().getColor(R.color.text_color_regular));
                    holder.replyText.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.splitLine.setBackgroundColor(0xFFD0E5F2);
                }

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            final Comment comment = getItem(position);
            final long t = comment.create_time * 1000L;
            final UserInfo user = comment.usr;
            mDate.setTime(t);

            mImageLoader.get(user.icon, ImageLoader.getImageListener(holder.avatarImage,
                    R.drawable.user_default, R.drawable.user_default));

            String formattedString = user.nickname + " " + (Constants.MODE_NIGHT == mTheme
                    ? "<font color='#666666'>" : "<font color='#999999'>") + mFormat.format(mDate) + "</font>";
            holder.userText.setText(Html.fromHtml(formattedString));

            String content = comment.contents;

            if (content.contains("quote")) {
                final Pattern pattern = Pattern.compile("(.*?)<div class='reply-quote'>(.*?)<\\/div>");
                Matcher matcher = pattern.matcher(content);

                String reply = "";
                while (matcher.find()) {
                    content = matcher.group(1);
                    reply = matcher.group(2);
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
    }

    private static class ViewHolder {
        public ImageView avatarImage;
        public TextView userText;
        public TextView contentText;
        public TextView replyText;
        public LinearLayout replyLayout;
        public View splitLine;
    }

    @Override
    protected void applyTheme(int theme) {
        super.applyTheme(theme);

        final Button sendBtn = (Button) findViewById(R.id.btn_send);
        final RelativeLayout replyView = (RelativeLayout) findViewById(R.id.comment_layout);
        final View split = findViewById(R.id.split_h_comment);

        if (Constants.MODE_NIGHT == theme) {
            sendBtn.setBackgroundResource(R.drawable.btn_gray_night);
            sendBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
            replyView.setBackgroundColor(0xFF1C1C1C);
            split.setBackgroundColor(0xFF303030);
            mEditText.setTextColor(getResources().getColor(R.color.text_color_weak));
            mEditText.setHintTextColor(0xFF666666);
        } else {
            sendBtn.setBackgroundResource(R.drawable.btn_gray);
            sendBtn.setTextColor(getResources().getColor(R.color.text_color_regular));
            replyView.setBackgroundColor(0xFFF6F6F6);
            split.setBackgroundColor(0xFFFFFFFF);
            mEditText.setTextColor(getResources().getColor(R.color.text_color_regular));
            mEditText.setHintTextColor(0xFF9C9C9C);
        }
    }
}
