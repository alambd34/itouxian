package com.itouxian.android.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.itouxian.android.App;
import com.itouxian.android.FileCache;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import volley.VolleyError;
import volley.toolbox.ImageLoader;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-18
 * Time: 上午11:06
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static void setErrorView(View errorView, int resId) {
        setErrorView(errorView, App.getInstance().getString(resId));
    }

    public static void setErrorView(View errorView, String msg) {
        final TextView errorText = (TextView) errorView.findViewById(R.id.tv_empty_tip);
        errorView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        errorText.setText(msg);
        final int mode = PrefsUtil.getThemeMode();
        errorText.setTextColor(mode == Constants.MODE_NIGHT ? 0xFF999999: 0xFF333333);
        errorText.setVisibility(View.VISIBLE);
    }


    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1;
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    public static boolean isLogin() {
        return null != PrefsUtil.getUser();
    }

    public static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dp + .5f);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void showToast(String msg) {
        Toast.makeText(App.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int resId) {
        showToast(App.getInstance().getString(resId));
    }
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidht, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        return newbmp;
    }

    public static synchronized boolean didNetworkConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != conn) {
            NetworkInfo info = conn.getActiveNetworkInfo();
            if (null != info) return info.isConnected();
        }
        return false;
    }

    public static CharSequence trim(Spanned spanned, int start, int end) {
        while (start < end && Character.isWhitespace(spanned.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(spanned.charAt(end - 1))) {
            end--;
        }

        return spanned.subSequence(start, end);
    }

    public static void shareText(Context context, String content) {
        content = content.replaceAll("<p>|<\\/p>|&(.*?);", "");
        content += " - 来自爱偷闲 iTouxian.Com";

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent,
                context.getString(R.string.share_from)));
    }

    public static void shareImage(final Context context, String url) {
        HttpUtils.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (null != bitmap) {
                    String path = FileCache.getTempCacheDir();
                    if (!new File(path).exists()) FileCache.mkDirs(path);

                    try {
                        File file = new File(path, "temp");
                        file.createNewFile();

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

                        byte[] data = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();

                        Uri uri = Uri.fromFile(file);

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, uri);

                        intent.setType("image/*");
                        context.startActivity(Intent.createChooser(intent,
                                context.getString(R.string.share_from)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    public static synchronized boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                int networkInfoType = networkInfo.getType();
                if (networkInfoType == ConnectivityManager.TYPE_WIFI || networkInfoType == ConnectivityManager.TYPE_ETHERNET) {
                    return networkInfo.isConnected();
                }
            }
        }
        return false;
    }

    public static String getVersionName(Context context) {
        String versionName = "";
        Context appContext = context.getApplicationContext();

        try {
            PackageManager pm = appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(appContext.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (TextUtils.isEmpty(s))
            return "";

        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        Context appContext = context.getApplicationContext();

        try {
            PackageManager pm = appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(appContext.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

    public static void runWithoutMessage(final Runnable action, final Runnable postAction) {
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                postAction.run();
            }
        };

        final Thread runner = new Thread(new Runnable() {
            public void run() {
                action.run();
                handler.sendEmptyMessage(0);
            }
        });
        runner.setPriority(Thread.MIN_PRIORITY);
        runner.start();
    }

    public static String readFromAssets(Context context, String name) {
        InputStream is;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            is = context.getAssets().open(name);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toString();
    }


}
