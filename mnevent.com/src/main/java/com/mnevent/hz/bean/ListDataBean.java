package com.mnevent.hz.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/12/13.
 */

public class ListDataBean implements Parcelable {

    private int image;

    private String texts;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTexts() {
        return texts;
    }

    public void setTexts(String texts) {
        this.texts = texts;
    }

    public int getPd() {
        return pd;
    }

    public void setPd(int pd) {
        this.pd = pd;
    }

    private int pd ;


    protected ListDataBean(Parcel in) {
        image = in.readInt();
        texts = in.readString();
        pd = in.readInt();
    }

    public static final Creator<ListDataBean> CREATOR = new Creator<ListDataBean>() {
        @Override
        public ListDataBean createFromParcel(Parcel in) {
            return new ListDataBean(in);
        }

        @Override
        public ListDataBean[] newArray(int size) {
            return new ListDataBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(image);
        dest.writeString(texts);
        dest.writeInt(pd);
    }
}
