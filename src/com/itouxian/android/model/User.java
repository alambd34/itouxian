package com.itouxian.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chenjishi on 14-3-15.
 */
public class User implements Parcelable {
    public String icon;
    public String alias;
    public String nickname;

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User() {
    }

    public User(Parcel in) {
        icon = in.readString();
        alias = in.readString();
        nickname = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(icon);
        dest.writeString(alias);
        dest.writeString(nickname);
    }
}
