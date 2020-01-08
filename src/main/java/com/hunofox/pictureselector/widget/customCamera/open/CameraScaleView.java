package com.hunofox.pictureselector.widget.customCamera.open;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/22 14:06.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public class CameraScaleView extends SurfaceView {

    /**
     * 记录是拖拉照片模式还是放大缩小照片模式
     */

    private static final int MODE_INIT = 0;
    /**
     * 放大缩小照片模式
     */
    private static final int MODE_ZOOM = 1;
    private int mode = MODE_INIT;// 初始状态

    private float startDis;

    private Camera mCamera;
    private int mZoom;

    public CameraScaleView(Context context) {
        super(context);
    }

    public CameraScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                mode = MODE_INIT;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //如果mZoomSeekBar为null 表示该设备不支持缩放 直接跳过设置mode Move指令也无法执行
                mode = MODE_ZOOM;
                /** 计算两个手指间的距离 */
                startDis = spacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_ZOOM) {
                    //只有同时触屏两个点的时候才执行
                    if (event.getPointerCount() < 2) return true;
                    float endDis = spacing(event);// 结束距离
                    //每变化10f zoom变1
                    int scale = (int) ((endDis - startDis) / 10f);
                    if (scale >= 1 || scale <= -1) {
                        int zoom = getZoom() + scale;
                        //zoom不能超出范围
                        if (zoom > getMaxZoom()) zoom = getMaxZoom();
                        if (zoom < 0) zoom = 0;
                        setZoom(zoom);
                        //将最后一次的距离设为当前距离
                        startDis = endDis;
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void setCamera(Camera camera){
        this.mCamera = camera;
    }

    public void setZoom(int zoom) {
        if (mCamera == null) return;
        Camera.Parameters parameters;
        //注意此处为录像模式下的setZoom方式。在Camera.unlock之后，调用getParameters方法会引起android框架底层的异常
        //stackoverflow上看到的解释是由于多线程同时访问Camera导致的冲突，所以在此使用录像前保存的mParameters。
        parameters = mCamera.getParameters();

        if (!parameters.isZoomSupported()) return;
        parameters.setZoom(zoom);
        mCamera.setParameters(parameters);
        mZoom = zoom;
    }

    public int getZoom(){
        return mZoom;
    }

    public int getMaxZoom() {
        if (mCamera == null) return -1;
        Camera.Parameters parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported()) return -1;
        return parameters.getMaxZoom() > 100 ? 100 : parameters.getMaxZoom();
    }

    private float spacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Float.parseFloat(String.valueOf(Math.sqrt(x * x + y * y)));
    }
}
