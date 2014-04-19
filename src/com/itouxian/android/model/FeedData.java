package com.itouxian.android.model;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-3-15.
 */
public class FeedData {
    public FeedModel data;

    public static class FeedModel {
        public int total;
        public ArrayList<Feed> data;
        public String msg;
        public int code;
    }
}