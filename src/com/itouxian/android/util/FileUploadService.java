package com.itouxian.android.util;

import android.os.Handler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenjishi on 14-5-8.
 */
public class FileUploadService {
    private static final String DEFAULT_FILE_KEY = "image";
    private HttpClient mHttpClient;
    private Handler mHandler;

    public FileUploadService() {
        mHttpClient = new DefaultHttpClient();
        mHandler = new Handler();
    }

    public void post(final String url, final File file, final Map<String, String> params) {
        post(url, file, params, null);
    }

    public void post(final String url,
                     final File file,
                     final Map<String, String> params,
                     final FileUploadCallback callback) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HttpPost httpPost = new HttpPost(url);

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
                StringBody stringBody;
                if (null != params) {
                    for (String key : params.keySet()) {
                        stringBody = new StringBody(params.get(key), contentType);
                        builder.addPart(key, stringBody);
                    }
                }

                if (null != file && file.exists()) {
                    FileBody fileBody = new FileBody(file);
                    builder.addPart(DEFAULT_FILE_KEY, fileBody);
                }

                final HttpEntity entity = builder.build();
                httpPost.setEntity(entity);

                int statusCode = -1;
                String responseBody;

                try {
                    HttpResponse httpResponse = mHttpClient.execute(httpPost);

                    StatusLine status = httpResponse.getStatusLine();
                    statusCode = status.getStatusCode();
                    responseBody = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

                    if (statusCode >= 300) {
                        sendFailure(callback, statusCode, responseBody, null);
                    } else {
                        sendSuccess(callback, statusCode, responseBody);
                    }
                } catch (IOException e) {
                    sendFailure(callback, statusCode, null, e);
                }
            }
        });
    }

    private void sendSuccess(final FileUploadCallback callback, final int statusCode, final String response) {
        if (null == callback) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(statusCode, response);
            }
        });
    }

    private void sendFailure(final FileUploadCallback callback, final int statusCode,
                             final String response, final Throwable e) {
        if (null == callback) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(statusCode, response, e);
            }
        });
    }
}
