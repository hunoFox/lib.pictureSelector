package com.hunofox.pictureselector.mvp.views;

import com.hunofox.pictureselector.base.PictureSelectView;
import com.hunofox.pictureselector.beans.FolderBean;
import com.hunofox.pictureselector.beans.PictureBean;

import java.util.List;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/26 14:11.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public interface IPictureView extends PictureSelectView {

    void notifyDataSetChanged(List<PictureBean> pics, List<FolderBean> folders);


}
