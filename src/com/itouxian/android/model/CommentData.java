package com.itouxian.android.model;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-5-25.
 */
public class CommentData {
    public CommentList data;

    public static class CommentList {
        public int total;
        public ArrayList<Comment> data;
    }
}
