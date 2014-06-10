package com.itouxian.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.util.Constants;

/**
 * Created by chenjishi on 14-5-6.
 */
public class CircleView extends View {
    private int mNumber;
    private float mDensity;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setNumber(int n) {
        mNumber = n;
        invalidate();
    }

    private void init(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;

        int mode = PrefsUtil.getThemeMode();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Constants.MODE_NIGHT == mode ? 0xFF666666 : Color.WHITE);

        mTextPaint.setColor(Constants.MODE_NIGHT == mode ? 0xFF1C1C1C : 0xFF0091C4);
        mTextPaint.setTextSize(mDensity * 8f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNumber == 0) return;

        int width = getWidth();
        float r = width / 2f;
        canvas.drawCircle(r, r, r, mPaint);

        float x = r;
        float y = r - (mTextPaint.descent() + mTextPaint.ascent()) / 2;
        canvas.drawText(String.valueOf(mNumber), x, y, mTextPaint);
    }
}
