package com.itouxian.android.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.itouxian.android.R;
import com.itouxian.android.view.SlidingLayout;
import com.nineoldandroids.view.ViewHelper;
import static com.itouxian.android.util.IntentUtils.KEY_PREVIEW_IMAGE;

/**
 * Created by chenjishi on 14-3-26.
 */
public class SlidingActivity extends BaseActivity {
    private static final float MIN_SCALE = 0.85f;

    private View mPreview;

    @SuppressLint("NewApi")
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        mRootView.removeAllViews();

        SlidingLayout slidingLayout = new SlidingLayout(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPreview = new ImageView(this);
        slidingLayout.addView(mPreview, lp);

        FrameLayout contentView = new FrameLayout(this);
        contentView.setBackgroundColor(0xFFF0F0F0);

        if (!mHideTitle) {
            int resId = -1 == mTitleResId ? R.layout.base_title_layout : mTitleResId;
            mInflater.inflate(resId, contentView);
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
        final int marginTop = mHideTitle ? 0 : (int) (mDensity * 48.f + .5f);
        layoutParams.setMargins(0, marginTop, 0, 0);
        contentView.addView(mInflater.inflate(layoutResID, null), layoutParams);

        slidingLayout.addView(contentView, lp);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final float initOffset = (1 - MIN_SCALE) * metrics.widthPixels / 2.f;

        slidingLayout.setSliderFadeColor(0x00000000);
        slidingLayout.setPanelSlideListener(new SlidingLayout.SimpleSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                final int sdkInt = Build.VERSION.SDK_INT;

                if (slideOffset <= 0) {
                    if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
                        mPreview.setScaleX(MIN_SCALE);
                        mPreview.setScaleY(MIN_SCALE);
                    } else {
                        ViewHelper.setScaleX(mPreview, MIN_SCALE);
                        ViewHelper.setScaleY(mPreview, MIN_SCALE);
                    }
                } else if (slideOffset < 1) {
                    // Scale the page down (between MIN_SCALE and 1)
                    float scaleFactor = MIN_SCALE + Math.abs(slideOffset) * (1 - MIN_SCALE);

                    if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
                        mPreview.setAlpha(slideOffset);
                        mPreview.setTranslationX(initOffset * (1 - slideOffset));
                        mPreview.setScaleX(scaleFactor);
                        mPreview.setScaleY(scaleFactor);
                    } else {
                        ViewHelper.setAlpha(mPreview, slideOffset);
                        ViewHelper.setTranslationX(mPreview, initOffset * (1 - slideOffset));
                        ViewHelper.setScaleX(mPreview, scaleFactor);
                        ViewHelper.setScaleY(mPreview, scaleFactor);
                    }
                } else {
                    if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
                        mPreview.setScaleX(1);
                        mPreview.setScaleY(1);
                        mPreview.setAlpha(1);
                        mPreview.setTranslationX(0);
                    } else {
                        ViewHelper.setScaleX(mPreview, 1);
                        ViewHelper.setScaleY(mPreview, 1);
                        ViewHelper.setAlpha(mPreview, 1);
                        ViewHelper.setTranslationX(mPreview, 0);
                    }
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });

        byte[] byteArray = getIntent().getByteArrayExtra(KEY_PREVIEW_IMAGE);
        if (null != byteArray && byteArray.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 2;
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);

            if (null != bmp) {
                ((ImageView) mPreview).setImageBitmap(bmp);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mPreview.setScaleX(MIN_SCALE);
                    mPreview.setScaleY(MIN_SCALE);
                } else {
                    ViewHelper.setScaleX(mPreview, MIN_SCALE);
                    ViewHelper.setScaleY(mPreview, MIN_SCALE);
                }
            } else {
                /** preview image captured fail, disable the slide back */
                slidingLayout.setSlideable(false);
            }
        } else {
            /** preview image captured fail, disable the slide back */
            slidingLayout.setSlideable(false);
        }

        mRootView.addView(slidingLayout, lp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /** recycle the bitmap */
        Drawable drawable = ((ImageView) mPreview).getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
            System.gc();
        }
    }
}
