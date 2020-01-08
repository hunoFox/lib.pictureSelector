package com.hunofox.pictureselector.listeners;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/11/21 10:52.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class VideoGestureDetector extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
        //若返回false只能响应长按事件
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //长按事件
    }


}
