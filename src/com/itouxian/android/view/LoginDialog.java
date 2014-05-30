package com.itouxian.android.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.activity.MainActivity;
import com.itouxian.android.activity.RegisterActivity;
import com.itouxian.android.model.UserInfo;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import volley.Response;
import volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenjishi on 13-12-20.
 */
public class LoginDialog extends Dialog implements View.OnClickListener, Response.Listener<String>, Response.ErrorListener {
    private OnLoginListener listener;
    private Context context;

    public LoginDialog(Context context, OnLoginListener listener) {
        super(context, R.style.FullHeightDialog);
        this.context = context;
        this.listener = listener;

        View view = LayoutInflater.from(context).inflate(R.layout.login, null);
        setContentView(view);

        view.findViewById(R.id.btn_register).setOnClickListener(this);
        view.findViewById(R.id.btn_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_login == v.getId()) {
            final String str1 = ((EditText) findViewById(R.id.user_name)).getText().toString().trim();
            final String str2 = ((EditText) findViewById(R.id.password)).getText().toString().trim();

            if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)) {
                Utils.showToast(context.getString(R.string.login_input_empty));
                return;
            }

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", str1);
            params.put("password", str2);

            HttpUtils.post("http://www.itouxian.com/json/login", params, this, this);
        } else {
            Intent intent = new Intent(context, RegisterActivity.class);
            ((Activity) context).startActivityForResult(intent, MainActivity.REQUEST_CODE_REGISTER);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        listener.onLoginError();
        dismiss();
    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jObj = new JSONObject(response);

                int code = jObj.optInt("code", -1);
                if (0 == code) {
                    JSONObject dataObj = jObj.getJSONObject("data");
                    UserInfo user = new UserInfo();

                    user.nickname = dataObj.optString("nickname", "");
                    user.sexStr = dataObj.optString("sex", "");
                    user.icon = dataObj.optString("icon", "");
                    user.token = dataObj.optString("token", "");

                    PrefsUtil.setUser(user);
                    listener.onLoginSuccess();
                } else {
                    listener.onLoginError();
                }
            } catch (JSONException e) {
                listener.onLoginError();
            }
        } else {
            listener.onLoginError();
        }

        dismiss();
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

    public interface OnLoginListener {

        public void onLoginSuccess();

        public void onLoginError();
    }
}
