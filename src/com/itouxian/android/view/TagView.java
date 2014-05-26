package com.itouxian.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.itouxian.android.util.Utils;

/**
 * Created by chenjishi on 14-5-24.
 */
public class TagView extends TextView {
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();

    private static final int BORDER_COLOR_NORMAL = 0xFFD0D0D0;
    private static final int BORDER_COLOR_SELECT = 0xFF3291DC;

    private static final int FILL_COLOR_NORMAL = 0xFFF1F1F1;
    private static final int FILL_COLOR_SELECT = 0xFFFFFFFF;

    private static final int TEXT_COLOR_NORMAL = 0xFF666666;

    private String mText = "";

    private boolean mSelected;

    public TagView(Context context) {
        this(context, null);
    }

    public TagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setTagText(String s) {
        mText = s;
        invalidate();
    }

    public void setSelected(boolean b) {
        mSelected = b;
        invalidate();
    }

    private void init(Context context) {
        int padding = Utils.dp2px(context, 8);
        setPadding(0, padding / 2, 0, padding / 2);

        mPaint.setColor(BORDER_COLOR_NORMAL);
        mPaint.setStyle(Paint.Style.FILL);

        float density = getResources().getDisplayMetrics().density;
        mTextPaint.setColor(TEXT_COLOR_NORMAL);
        mTextPaint.setTextSize(density * 14.f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (TextUtils.isEmpty(mText)) return;

        int w = getWidth();
        int h = getHeight();

        mPaint.setColor(mSelected ? BORDER_COLOR_SELECT : BORDER_COLOR_NORMAL);
        float y = h - h / 4f;
        float x = w - w / 8f;
        mPath.moveTo(0, 0);
        mPath.lineTo(w, 0);
        mPath.lineTo(w, y);
        mPath.lineTo(x, h);
        mPath.lineTo(0, h);
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        mPaint.setColor(mSelected ? FILL_COLOR_SELECT : FILL_COLOR_NORMAL);
        mPath.reset();
        mPath.moveTo(3, 3);
        mPath.lineTo(w - 3, 3);
        mPath.lineTo(w - 3, y - 3);
        mPath.lineTo(x - 3, h - 3);
        mPath.lineTo(3, h - 3);
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        mTextPaint.setColor(mSelected ? BORDER_COLOR_SELECT : TEXT_COLOR_NORMAL);
        float xPos = w / 2f;
        float yPos = h / 2f - (mTextPaint.descent() + mTextPaint.ascent()) / 2;
        canvas.drawText(mText, xPos, yPos, mTextPaint);
    }
}
