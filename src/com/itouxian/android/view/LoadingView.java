package com.itouxian.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;

import static com.itouxian.android.util.Constants.MODE_NIGHT;

/**
 * Created by chenjishi on 14-6-3.
 */
public class LoadingView extends FrameLayout {
    private ProgressBar progressBar;
    private TextView textView;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        int theme = PrefsUtil.getThemeMode();
        setBackgroundResource(theme == MODE_NIGHT ? R.color.background_night : R.color.background);

        float density = getResources().getDisplayMetrics().density;

        int size = (int) (density * 30);
        int marginTop = -(int) (density * 80);
        LayoutParams lp = new LayoutParams(size, size, Gravity.CENTER);
        lp.setMargins(0, marginTop, 0, 0);
        progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(lp);
        progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_bar));
        addView(progressBar);

        LayoutParams lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        lp2.setMargins(0, marginTop, 0, 0);
        textView = new TextView(context);
        textView.setText(R.string.loading);
        textView.setTextColor(theme == MODE_NIGHT ? 0xFF999999 : 0xFF333333);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.f);
        textView.setPadding(0, (int) (density * 4), 0, 0);
        textView.setLayoutParams(lp2);
        textView.setVisibility(GONE);
        addView(textView);
    }

    public void setErrorTips(String s) {
        progressBar.setVisibility(GONE);
        textView.setText(s);
        textView.setVisibility(VISIBLE);
    }
}
