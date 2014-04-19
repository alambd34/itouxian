package com.itouxian.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by chenjishi on 14-3-30.
 */
public class FitImageView extends ImageView {
    private int mWidth;

    public FitImageView(Context context) {
        super(context);
    }

    public FitImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        Drawable drawable = getDrawable();
        if (null != drawable) {
            int height = width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();

            if (height >= 1000) height = 500;

            setMeasuredDimension(width, height);
        }
    }
}
