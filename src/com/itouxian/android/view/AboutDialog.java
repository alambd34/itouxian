package com.itouxian.android.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.itouxian.android.R;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import volley.Response;
import volley.VolleyError;

/**
 * Created by chenjishi on 14-1-10.
 */
public class AboutDialog extends Dialog implements View.OnClickListener, Response.Listener<String>,
        Response.ErrorListener {
    private AboutDialogListener listener;
    private int count;

    public AboutDialog(Context context, AboutDialogListener listener) {
        super(context, R.style.FullHeightDialog);

        this.listener = listener;
        setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.about_view, null);
        setContentView(view);

        Button versionBtn = (Button) view.findViewById(R.id.btn_version);
        String versionName = Utils.getVersionName(context);
        if (null != versionName) {
            versionBtn.setText(versionName);
        }

        versionBtn.setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);

        HttpUtils.get("http://pan.baidu.com/s/1ntJTeBZ", this, this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_version) {
            count++;
            if (count == 5) {
                Utils.showToast("再点击两次有惊喜哦~");
                return;
            }

            if (count == 7) {
                listener.onVersionClicked();
                count = 0;
                dismiss();
            }
        } else {
            dismiss();
        }
    }

    @Override
    public void show() {
        WindowManager windowManager = getWindow().getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (metrics.widthPixels * 0.8f);
        getWindow().setAttributes(layoutParams);

        super.show();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i("test", "# error " + error);


    }

    @Override
    public void onResponse(String response) {
        Log.i("test", "# response " + response);

    }

    public interface AboutDialogListener {
        public void onVersionClicked();
    }
}
