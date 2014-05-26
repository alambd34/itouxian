package com.itouxian.android.util;

/**
 * Created by chenjishi on 14-5-8.
 */
public interface FileUploadCallback {

    public void onSuccess(int statusCode, String response);

    public void onFailure(int statusCode, String response, Throwable e);

}
