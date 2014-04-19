package com.itouxian.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.itouxian.android.R;
import com.itouxian.android.util.HttpUtils;
import volley.Response;
import volley.VolleyError;

/**
 * Created by chenjishi on 14-4-2.
 */
public class GifView extends ImageView implements Response.Listener<byte[]>, Response.ErrorListener {
    private Movie mMovie;
    private long mMovieStart;

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private int mWidth;

    public void setGifImageUrl(String url, int width) {
//        HttpUtils.getByteArray(url, this, this);
        HttpUtils.getByteArray("http://img1.itouxian.com/2014/4/images/1396418594699.gif", this, this);
        mWidth = width;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setBackgroundResource(R.drawable.icon);
    }

    @Override
    public void onResponse(byte[] response) {
        if (null != response && response.length > 0) {
            mMovie = Movie.decodeByteArray(response, 0, response.length);

            final int w = mMovie.width();
            final int h = mMovie.height();
            final int height = mWidth * h / w;

            Log.i("test", "duration " + mMovie.duration());

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mWidth, height);
            setLayoutParams(layoutParams);
            /** not gif image so we just display one frame */
            if (mMovie.duration() <= 100) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length);
                setImageBitmap(bitmap);
            } else {
                invalidate();
            }
        } else {
            setBackgroundResource(R.drawable.icon);
        }
    }

    @SuppressWarnings("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && canvas.isHardwareAccelerated()) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        super.onDraw(canvas);

        int dur = mMovie.duration();
        if (null == mMovie || mMovie.duration() < 100) return;

        final int w = mMovie.width();
        final int h = mMovie.height();

        final float scaleX = mWidth * 1f / w;
        final float scaleY = (mWidth * h * 1f / w) / h;
        canvas.scale(scaleX, scaleY);

        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }

        if (dur < 1000) dur = 1000;

        int relTime = (int) ((now - mMovieStart) % dur);
        Log.i("test", "relTime " + relTime);
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 0, 0);
        invalidate();
    }
}
