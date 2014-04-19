package com.itouxian.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chenjishi on 14-3-15.
 */
public class FeedTag implements Parcelable {
    private long id;
    private String tag_name;

    public static final Creator<FeedTag> CREATOR = new Creator<FeedTag>() {
        @Override
        public FeedTag createFromParcel(Parcel source) {
            return new FeedTag(source);
        }

        @Override
        public FeedTag[] newArray(int size) {
            return new FeedTag[size];
        }
    };

    public FeedTag() {

    }

    public FeedTag(Parcel in) {
        id = in.readLong();
        tag_name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(tag_name);
    }
}
