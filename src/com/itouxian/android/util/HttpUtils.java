package com.itouxian.android.util;

import android.app.ActivityManager;
import android.content.Context;
import volley.*;
import volley.toolbox.*;

import java.util.Map;

/**
 * Created by chenjishi on 14-3-15.
 */
public class HttpUtils {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(context));
    }

    public static RequestQueue getRequestQueue() {
        if (null != mRequestQueue) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static ImageLoader getImageLoader() {
        if (null != mImageLoader) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    public static String getSync(String url) {
        RequestFuture<String> f = RequestFuture.newFuture();
        StringRequest req = new StringRequest(url, f, f);
        RequestQueue queue = getRequestQueue();
        queue.add(req);

        try {
            String response = f.get();
            return response;
        } catch (Exception e) {
            return "";
        }
    }

    public static void get(String url,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        queue.add(new StringRequest(url, listener, errorListener));
    }

    public static void post(String url,
                            final Map<String, String> params,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        /** prevent retry by set retry number to 0 */
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        queue.add(request);
    }

    public static <T> void get(String url,
                               Class<T> clazz,
                               Response.Listener<T> listener,
                               Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        GsonRequest<T> request = new GsonRequest<T>(url, clazz, listener, errorListener);
        queue.add(request);
    }

    public static void getByteArray(String url,
                                    Response.Listener<byte[]> listener,
                                    Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        queue.add(new ByteArrayRequest(url, listener, errorListener));
    }
}
