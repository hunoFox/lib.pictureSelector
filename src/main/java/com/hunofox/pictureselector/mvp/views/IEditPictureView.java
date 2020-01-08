package com.hunofox.pictureselector.mvp.views;


import com.hunofox.pictureselector.base.PictureSelectView;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/28 15:55.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public interface IEditPictureView extends PictureSelectView {

    void onSaveImageSuccess(String path);

    void onSingleClick();

}
