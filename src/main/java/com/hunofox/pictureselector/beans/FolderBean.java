package com.hunofox.pictureselector.beans;

import android.text.TextUtils;

import java.util.List;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/26 15:13.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public class FolderBean {

    public String folderName;
    public String folderPath;
    public PictureBean picture;
    public List<PictureBean> pics;

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FolderBean){
            return TextUtils.equals(this.folderPath, ((FolderBean) obj).folderPath);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "FolderBean{" +
                "folderName='" + folderName + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", picture=" + picture +
                ", pics=" + pics +
                '}';
    }
}
