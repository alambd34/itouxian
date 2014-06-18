package com.itouxian.android.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.model.Feed;
import com.itouxian.android.sina.RequestListener;
import com.itouxian.android.sina.StatusesAPI;
import com.itouxian.android.util.FileUtils;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.*;
import volley.VolleyError;
import volley.toolbox.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by chenjishi on 14-6-17.
 */
public class ShareDialog extends Dialog implements View.OnClickListener, RequestListener {
    private static final int THUMB_SIZE = 100;

    private static final String WX_APP_ID = "wxd9a0b27f930ca42e";
    private static final String WB_APP_ID = "554729938";

    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    private static final int SHARE_TO_SESSION = 100;
    private static final int SHARE_TO_FRIENDS = 101;
    private static final int SHARE_TO_WEIBO = 102;

    private Context mContext;

    private IWXAPI mWXAPI;
    private Feed mFeed;

    public ShareDialog(Context context) {
        super(context, R.style.FullHeightDialog);

        mContext = context;

        setCanceledOnTouchOutside(true);

        mWXAPI = WXAPIFactory.createWXAPI(context, WX_APP_ID);
        mWXAPI.registerApp(WX_APP_ID);

        int paddingTop = Utils.dp2px(context, 12.f);
        LinearLayout container = new LinearLayout(context);
        container.setBackgroundColor(0xFFFFFFFF);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(3.f);
        container.setPadding(0, paddingTop, 0, paddingTop);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int[] iconIds = {R.drawable.ic_session, R.drawable.ic_friend, R.drawable.ic_weibo};
        String[] names = {"微信好友", "微信朋友圈", "新浪微博"};

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1.f;

        int padding = Utils.dp2px(context, 8);
        for (int i = 0; i < iconIds.length; i++) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(lp);
            tv.setCompoundDrawablesWithIntrinsicBounds(0, iconIds[i], 0, 0);
            tv.setCompoundDrawablePadding(padding);
            tv.setPadding(padding, padding, padding, padding);
            tv.setBackgroundResource(R.drawable.highlight_color);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.f);
            tv.setTextColor(0xFF000000);
            tv.setText(names[i]);
            tv.setTag(SHARE_TO_SESSION + i);
            tv.setOnClickListener(this);

            container.addView(tv);
        }

        setContentView(container);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = context.getResources().getDisplayMetrics().widthPixels;
        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(layoutParams);
    }

    public void setShareData(Feed feed) {
        mFeed = feed;
    }

    private void shareToWX(int type) {
        String imageUrl = mFeed.imageUrl;
        if (!TextUtils.isEmpty(imageUrl)) {
            sendImage(type, imageUrl);
        } else {
            sendText(type, getContent());
        }
    }

    private void shareToWB() {
        Oauth2AccessToken token = PrefsUtil.getAccessToken();

        String content = getContent();
        String imageUrl = mFeed.imageUrl;
        if (!token.isSessionValid()) {
            authorize(content, imageUrl);
        } else {
            StatusesAPI api = new StatusesAPI(token);
            api.uploadUrlText(content, imageUrl, null, null, null, this);
        }
    }

    private void sendImage(final int type, String url) {
        HttpUtils.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (null != bitmap) {
                    sendImage(type, bitmap);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(R.string.share_image_fail);
            }
        });
    }

    private void sendImage(int type, Bitmap bitmap) {
        String title = getTitle();

        WXImageObject imageObject = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage(imageObject);
        msg.title = title;
        msg.description = title;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int dstWidth;
        int dstHeight;

        if (w < h) {
            dstHeight = THUMB_SIZE;
            dstWidth = (w * THUMB_SIZE) / h;
        } else {
            dstWidth = THUMB_SIZE;
            dstHeight = (h * THUMB_SIZE) / w;
        }

        Bitmap thumb = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
        msg.thumbData = FileUtils.bmpToByteArray(thumb, true);

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = "image" + System.currentTimeMillis();
        request.message = msg;
        request.scene = type;

        mWXAPI.sendReq(request);
    }

    private void sendText(int type, String content) {
        String title = getTitle();

        WXTextObject textObject = new WXTextObject(content);
        WXMediaMessage msg = new WXMediaMessage(textObject);
        msg.title = title;
        msg.description = title;

        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.transaction = "text" + System.currentTimeMillis();
        request.message = msg;
        request.scene = type;

        mWXAPI.sendReq(request);
    }

    private void authorize(final String content, final String imageUrl) {
        WeiboAuth weiboAuth = new WeiboAuth(mContext, WB_APP_ID, REDIRECT_URL, SCOPE);
        weiboAuth.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                PrefsUtil.saveAccessToken(accessToken);

                Log.i("test", accessToken.toString());
                Log.i("test", "token  " + accessToken.getToken());
                Log.i("test", "expire " + accessToken.getExpiresTime());

                StatusesAPI api = new StatusesAPI(accessToken);
                api.uploadUrlText(content, imageUrl, null, null, null, ShareDialog.this);
            }

            @Override
            public void onWeiboException(WeiboException e) {

            }

            @Override
            public void onCancel() {

            }
        }, WeiboAuth.OBTAIN_AUTH_CODE);
    }

    private String getContent() {
        String separator = System.getProperty("line.separator");

        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(mFeed.title)) {
            builder.append(mFeed.title)
                    .append(separator)
                    .append(separator);
        }

        String content = mFeed.contents.replaceAll("<p>|<\\/p>|&(.*?);", "");
        content = content.replaceAll("<br(.*?)>", separator);

        builder.append(content);

        return builder.toString();
    }

    private String getTitle() {
        String title;
        title = mFeed.title;
        if (TextUtils.isEmpty(title)) {
            String content = mFeed.contents.replaceAll("<p>|<\\/p>|&(.*?);", "");
            content = content.replaceAll("<br(.*?)>", "");

            title = content;
            if (title.length() > 12) {
                title = title.substring(0, 12);
            }
        }

        return title;
    }

    @Override
    public void onClick(View v) {
        int tag = (Integer) v.getTag();

        switch (tag) {
            case SHARE_TO_SESSION:
                shareToWX(SendMessageToWX.Req.WXSceneSession);
                break;
            case SHARE_TO_FRIENDS:
                shareToWX(SendMessageToWX.Req.WXSceneTimeline);
                break;
            case SHARE_TO_WEIBO:
                shareToWB();
                break;
        }

        dismiss();
    }

    @Override
    public void onComplete(String response) {
        Log.i("test", "response " + response);

    }

    @Override
    public void onComplete4binary(ByteArrayOutputStream responseOS) {

    }

    @Override
    public void onIOException(IOException e) {
        Log.i("test", "onIOException " + e);


    }

    @Override
    public void onError(WeiboException e) {
        Log.i("test", "WeiboException " + e);


    }
}
