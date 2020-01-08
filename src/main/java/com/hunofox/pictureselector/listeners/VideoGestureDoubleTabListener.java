package com.hunofox.pictureselector.listeners;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/11/21 10:56.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class VideoGestureDoubleTabListener implements GestureDetector.OnDoubleTapListener {

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        //单击事件
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //双击事件
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        //双击按下、移动，手指离开时调用
        return false;
    }
}
