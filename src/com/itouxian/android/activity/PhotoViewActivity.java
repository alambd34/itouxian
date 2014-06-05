package com.itouxian.android.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.itouxian.android.R;
import com.itouxian.android.util.DepthPageTransformer;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.TouchImageView;
import volley.VolleyError;
import volley.toolbox.ImageLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chenjishi on 14-5-29.
 */
public class PhotoViewActivity extends Activity implements ViewPager.OnPageChangeListener {
    private ArrayList<String> mImageList = new ArrayList<String>();

    private int mCurrentIndex;

    private ViewPager mViewPager;
    private RelativeLayout mToolBar;

    private ImageLoader imageLoader;

    private boolean showToolBar = false;

    private int mTouchSlop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int sdk_int = Build.VERSION.SDK_INT;
        if (sdk_int < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();

            if (sdk_int >= 19) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        setContentView(R.layout.activity_photo_viewer);

        Bundle bundle = getIntent().getExtras();
        if (null == bundle) return;

        mImageList = bundle.getStringArrayList("images");
        String currentUrl = bundle.getString("imgsrc");

        for (int i = 0; i < mImageList.size(); i++) {
            if (mImageList.get(i).equals(currentUrl)) {
                mCurrentIndex = i;
                break;
            }
        }

        imageLoader = HttpUtils.getImageLoader();

        mToolBar = (RelativeLayout) findViewById(R.id.tool_bar);
        mViewPager = (ViewPager) findViewById(R.id.pager_photo);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setAdapter(new PhotoPagerAdapter(this));
        mViewPager.setOnPageChangeListener(this);
    }

    private float mDownX;
    private float mDownY;
    private boolean isOnClick;
    private Rect mRect = new Rect();
    private int[] mLocation = new int[2];

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                isOnClick = true;

                mToolBar.getLocationOnScreen(mLocation);
                mRect.left = mLocation[0];
                mRect.top = mLocation[1];
                mRect.right = mLocation[0] + mToolBar.getWidth();
                mRect.bottom = mLocation[1] + mToolBar.getHeight();

                if (mRect.contains((int) mDownX, (int) mDownY)) {
                    isOnClick = false;
                    return super.dispatchTouchEvent(ev);
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isOnClick) {
                    onTapUp();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isOnClick && (Math.abs(mDownX - ev.getX()) > mTouchSlop ||
                        Math.abs(mDownY - ev.getY()) > mTouchSlop)) {
                    isOnClick = false;
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(mCurrentIndex);
    }

    public void onCloseButtonClicked(View v) {
        finish();
    }

    public void onDownloadButtonClicked(View v) {
        saveImage();
    }

    public void onShareButtonClicked(View v) {
        String url = mImageList.get(mCurrentIndex);
        if (TextUtils.isEmpty(url)) return;

        Utils.shareImage(this, url);
    }

    private void onTapUp() {
        showToolBar = !showToolBar;
        mToolBar.setVisibility(showToolBar ? View.VISIBLE : View.GONE);
        float height = getResources().getDimension(R.dimen.action_bar_height);
        float startY = showToolBar ? 0 : height;
        float endY = showToolBar ? height : 0;
        Animation animation = new TranslateAnimation(0, 0, startY, endY);
        animation.setDuration(400);
        animation.setFillAfter(true);
        mToolBar.clearAnimation();
        mToolBar.startAnimation(animation);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        mCurrentIndex = i;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private void saveImage() {
        final String imageUrl = mImageList.get(mCurrentIndex);
        if (TextUtils.isEmpty(imageUrl)) return;

        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                String picUrl = null;
                Bitmap bitmap = response.getBitmap();

                if (null != bitmap) {
                    String name = System.currentTimeMillis() + ".jpg";
                    ContentResolver cr = PhotoViewActivity.this.getContentResolver();
                    picUrl = MediaStore.Images.Media.insertImage(cr, bitmap, name, "Image Saved From iTouxian");

                    if (!TextUtils.isEmpty(picUrl)) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        String imagePath = getFilePathByContentResolver(Uri.parse(picUrl));
                        Uri uri = Uri.fromFile(new File(imagePath));
                        intent.setData(uri);
                        PhotoViewActivity.this.sendBroadcast(intent);
                    }
                }

                Utils.showToast(getString(TextUtils.isEmpty(picUrl) ?
                        R.string.image_save_fail : R.string.image_save_success));
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(getString(R.string.image_save_fail));
            }
        });
    }

    private String getFilePathByContentResolver(Uri uri) {
        if (null == uri) return null;

        Cursor c = getContentResolver().query(uri, null, null, null, null);
        String filePath = null;
        if (null == c) {
            throw new IllegalArgumentException(
                    "Query on " + uri + " returns null result.");
        }
        try {
            if ((c.getCount() != 1) || !c.moveToFirst()) {
            } else {
                filePath = c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
        } finally {
            c.close();
        }
        return filePath;
    }

    private class PhotoPagerAdapter extends PagerAdapter {
        private Context mContext;

        public PhotoPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            FrameLayout viewGroup = new FrameLayout(mContext);

            TextView textView = new TextView(mContext);
            textView.setText(R.string.picture_loading);
            textView.setTextColor(getResources().getColor(R.color.text_color_summary));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.f);
            textView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            viewGroup.addView(textView);

            TouchImageView imageView = new TouchImageView(mContext);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            viewGroup.addView(imageView);

            imageLoader.get(mImageList.get(position), ImageLoader.getImageListener(imageView, R.drawable.gray,
                    R.drawable.gray));

            container.addView(viewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            return viewGroup;
        }

        @Override
        public int getCount() {
            return null == mImageList ? 0 : mImageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
