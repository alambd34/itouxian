package com.itouxian.android.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import volley.Response;
import volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import static com.itouxian.android.util.Constants.MODE_NIGHT;
import static com.itouxian.android.util.Constants.URL_COMMENTS;

/**
 * Created by chenjishi on 14-6-19.
 */
public class CommentPostView extends RelativeLayout implements Response.Listener<String>,
        Response.ErrorListener, View.OnClickListener, LoginDialog.OnLoginListener {
    private EditText mEditText;
    private ProgressDialog mProgress;
    private LoginDialog mLoginDialog;

    private Context mContext;

    private String mContent;
    private long mFeedId;
    private long mReplyId;

    private CommentCallback mCallback;

    public CommentPostView(Context context) {
        super(context);
        init(context);
    }

    public CommentPostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommentPostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setCallback(CommentCallback callback) {
        mCallback = callback;
    }

    public void setCommentId(long id) {
        mFeedId = id;
    }

    public void setReplyId(long replyId, String name) {
        mReplyId = replyId;
        mEditText.setHint(mContext.getString(R.string.reply, name));
        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void init(Context context) {
        mContext = context;

        LayoutInflater.from(context).inflate(R.layout.comment_post, this);

        Button button = (Button) findViewById(R.id.btn_send);
        mEditText = (EditText) findViewById(R.id.et_content);
        View splitView = findViewById(R.id.split_h_comment);

        button.setOnClickListener(this);

        Resources res = getResources();
        if (MODE_NIGHT == PrefsUtil.getThemeMode()) {
            button.setBackgroundResource(R.drawable.btn_gray_night);
            button.setTextColor(res.getColor(R.color.text_color_summary));

            mEditText.setTextColor(res.getColor(R.color.text_color_weak));
            mEditText.setHintTextColor(0xFF666666);

            splitView.setBackgroundColor(0xFF303030);
        } else {
            button.setBackgroundResource(R.drawable.btn_gray);
            button.setTextColor(res.getColor(R.color.text_color_regular));

            mEditText.setTextColor(res.getColor(R.color.text_color_regular));
            mEditText.setHintTextColor(0xFF9C9C9C);

            splitView.setBackgroundColor(0xFFFFFFFF);
        }
    }

    @Override
    public void onLoginSuccess() {
        getContentAndSend();
    }

    @Override
    public void onLoginError() {

    }

    @Override
    public void onClick(View v) {
        if (!Utils.isLogin()) {
            if (null == mLoginDialog) {
                mLoginDialog = new LoginDialog(getContext(), this);
            }
            mLoginDialog.show();
            return;
        }

        getContentAndSend();
    }

    private void getContentAndSend() {
        String content = mEditText.getText().toString();

        if (!TextUtils.isEmpty(content)) {
            sendComment(content);
            mReplyId = 0;
        } else {
            Utils.showToast(R.string.comment_empty);
        }
    }

    private void sendComment(String content) {
        if (content.equals(mContent)) return;

        Context context = getContext();
        if (null == mProgress) {
            mProgress = new ProgressDialog(context);
        }
        mProgress.setMessage(mContext.getString(R.string.comment_submitting));
        mProgress.show();

        UserInfo userInfo = PrefsUtil.getUser();
        if (null == userInfo) return;

        String token = userInfo.token;

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(mFeedId));
        params.put("token", token);
        params.put("content", context.getString(R.string.comment_from, content));
        params.put("review_id", String.valueOf(mReplyId));

        HttpUtils.post(URL_COMMENTS, params, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mProgress.dismiss();
        Utils.showToast(R.string.comment_fail);
    }

    @Override
    public void onResponse(String response) {
        mProgress.dismiss();
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jObj = new JSONObject(response);
                int code = jObj.optInt("code", -1);

                if (0 == code) {
                    mEditText.setText("");
                    mEditText.setHint("");
                    mEditText.clearFocus();
                    mContent = mEditText.getText().toString();
                    Utils.showToast(R.string.comment_success);
                    if (null != mCallback) {
                        mCallback.onCommentPostSuccess();
                    }
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
}
