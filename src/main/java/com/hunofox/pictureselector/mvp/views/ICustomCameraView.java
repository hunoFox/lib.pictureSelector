package com.hunofox.pictureselector.mvp.views;


import android.graphics.Bitmap;
import android.view.SurfaceView;

import com.hunofox.pictureselector.base.PictureSelectView;

import java.io.File;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/11 15:38.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public interface ICustomCameraView extends PictureSelectView {

    //必须用surfaceView或它的子类作为相机预览界面
    SurfaceView getSurfaceView();

    //拍照成功后回调
    void onTakePhotoSuccess(Bitmap bitmap);

    //调用bitmap保存图片的方法，保存图片成功后回调
    void onSaveBitmapSuccess(File imageFile);

    //调用bitmap保存图片的方法，保存图片失败后回调
    void onSaveBitmapFailed(String msg);
}
