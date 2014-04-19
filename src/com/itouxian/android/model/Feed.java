package com.itouxian.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.itouxian.android.util.ConstantUtil;

import java.util.ArrayList;

/**
 * Created by chenjishi on 14-3-15.
 */
public class Feed implements Parcelable {
    public long id;
    public String contents;
    public int status;
    public long create_time;
    public long uid;
    public int is_private;
    public String tags;
    public int deny_review;
    public int count_review;
    public String title;
    public String imageUrl;
    public int is_index;
    public int is_deny_autoindex;
    public int count_support;
    public int count_tread;
    public int is_ptop;
    public String tids;
    public int is_top;
    public int is_now;
    public int is_cut;
    public long commend_time;
    public int count_favourite;
    public ArrayList<FeedTag> formatTag = new ArrayList<FeedTag>();
    public int count_browse;
    public long update_time;
    public long editor_uid;
    public User usr;
    public int count_hold;
    public int feed_type = ConstantUtil.FEED_UNKNOWN;

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

    public Feed() {

    }

    public Feed(Parcel in) {
        id = in.readLong();
        contents = in.readString();
        status = in.readInt();
        create_time = in.readLong();
        uid = in.readLong();
        is_private = in.readInt();
        tags = in.readString();
        deny_review = in.readInt();
        count_review = in.readInt();
        title = in.readString();
        imageUrl = in.readString();
        is_index = in.readInt();
        is_deny_autoindex = in.readInt();
        count_support = in.readInt();
        count_tread = in.readInt();
        is_ptop = in.readInt();
        tids = in.readString();
        is_top = in.readInt();
        is_now = in.readInt();
        is_cut = in.readInt();
        commend_time = in.readLong();
        count_favourite = in.readInt();
        in.readTypedList(formatTag, FeedTag.CREATOR);
        count_browse = in.readInt();
        update_time = in.readLong();
        editor_uid = in.readLong();
        usr = in.readParcelable(User.class.getClassLoader());
        count_hold = in.readInt();
        feed_type = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(contents);
        dest.writeInt(status);
        dest.writeLong(create_time);
        dest.writeLong(uid);
        dest.writeInt(is_private);
        dest.writeString(tags);
        dest.writeInt(deny_review);
        dest.writeInt(count_review);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeInt(is_index);
        dest.writeInt(is_deny_autoindex);
        dest.writeInt(count_support);
        dest.writeInt(count_tread);
        dest.writeInt(is_ptop);
        dest.writeString(tids);
        dest.writeInt(is_top);
        dest.writeInt(is_now);
        dest.writeInt(is_cut);
        dest.writeLong(commend_time);
        dest.writeInt(count_favourite);
        dest.writeTypedList(formatTag);
        dest.writeInt(count_browse);
        dest.writeLong(update_time);
        dest.writeLong(editor_uid);
        dest.writeParcelable(usr, flags);
        dest.writeInt(count_hold);
        dest.writeInt(feed_type);
    }
}
