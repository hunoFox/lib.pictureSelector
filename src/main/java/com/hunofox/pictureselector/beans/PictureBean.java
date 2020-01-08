package com.hunofox.pictureselector.beans;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import androidx.annotation.NonNull;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/26 14:44.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public class PictureBean implements Parcelable{

    public String imagePath;//图片路径，包含图片文件名
    public String imageName;//图片名称
    public long imageDate;//图片日期
    public long imageSize;//图片尺寸

    public String desc;
    public String isVideo;

    public boolean isChecked;

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PictureBean){
            return TextUtils.equals(this.imagePath, ((PictureBean) obj).imagePath);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "PictureBean{" +
                "imagePath='" + imagePath + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageDate=" + imageDate +
                ", imageSize=" + imageSize +
                '}';
    }

    public PictureBean(){}

    protected PictureBean(Parcel in) {
        imagePath = in.readString();
        imageName = in.readString();
        imageDate = in.readLong();
        imageSize = in.readLong();
        desc = in.readString();
        isVideo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(imagePath);
        dest.writeString(imageName);
        dest.writeLong(imageDate);
        dest.writeLong(imageSize);
        dest.writeString(desc);
        dest.writeString(isVideo);
    }

    public static final Creator<PictureBean> CREATOR = new Creator<PictureBean>() {
        @Override
        public PictureBean createFromParcel(Parcel in) {
            return new PictureBean(in);
        }

        @Override
        public PictureBean[] newArray(int size) {
            return new PictureBean[size];
        }
    };
}
