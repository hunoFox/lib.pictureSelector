package com.hunofox.pictureselector.mvp.views;

import android.content.Intent;

import com.hunofox.pictureselector.base.PictureSelectView;


/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/21 9:29.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public interface ICameraView extends PictureSelectView {

    /**
     * 开启相机界面
     *
     * @param intent 已封装好所有进入相机界面需要配置的intent
     */
    void startCameraActivityForResult(Intent intent);

    /**
     * 最终拍照返回结果
     *
     * @param path 拍照存储路径
     */
    void cameraResult(String path);

}
