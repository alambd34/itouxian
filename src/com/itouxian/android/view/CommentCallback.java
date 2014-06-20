package com.itouxian.android.view;

/**
 * Created by chenjishi on 14-6-19.
 */
public interface CommentCallback {

    public void onCommentPostSuccess();

    public void onCommentError(String s);

    public void onCommentClicked(long id, String name);
}
