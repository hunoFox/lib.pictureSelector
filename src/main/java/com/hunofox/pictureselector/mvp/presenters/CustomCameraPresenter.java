package com.hunofox.pictureselector.mvp.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;

import com.hunofox.pictureselector.base.PictureSelectorPresenter;
import com.hunofox.pictureselector.mvp.views.ICustomCameraView;
import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.customCamera.AmbientLightManager;
import com.hunofox.pictureselector.widget.customCamera.CameraManager;
import com.hunofox.pictureselector.widget.customCamera.open.CameraFacing;
import com.hunofox.pictureselector.widget.customCamera.open.CameraScaleView;
import com.hunofox.pictureselector.widget.customCamera.open.OpenCamera;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;
import static com.hunofox.pictureselector.base.PictureSelector.SAVE_PATH;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/21 9:06.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 *
 * 使用方法：
 *
 * 1. 建立自定义相机Activity，在Activity的onCreate()中创建该Presenter实例
 *
 * 2. 分别在Activity相应的生命周期方法中调用Presenter相应的生命周期方法
 *
 * 注意：
 * 在跳转到该Activity前需要检查两项权限：
 * 1. 相机权限
 * 2. 读写SD卡权限
 * ----------------------------------------------------------------------------------------------------
 */
public class CustomCameraPresenter extends PictureSelectorPresenter<ICustomCameraView> implements SurfaceHolder.Callback{

    private static final int SAVE_FAILED = 384;
    private static final int SAVE_SUCCESS = 254;

    private CameraManager cameraManager;
    private final AmbientLightManager ambientLightManager;
    private boolean hasSurface = false;
    private CameraFacing facing = CameraFacing.BACK;
    private final CameraHandler handler;
    private boolean supportCameraPreviewScale = false;

    private boolean isAutoSavePhoto = false;
    private String folderName;
    private FileUtils.SaveImageResult saveImageResult;

    private int customWidth = -1;
    private int customHeight = -1;

    public CustomCameraPresenter(ICustomCameraView view) {
        super(view);

        ambientLightManager = new AmbientLightManager(CONTEXT);
        handler = new CameraHandler(this);
    }

    /**
     * 开启相机onResume()
     *
     * 该方法只能在onResume()中调用
     */
    public void onResume(){
        if(cameraManager == null){
            cameraManager = new CameraManager(CONTEXT);
        }
        cameraManager.setAutoSavePhoto(isAutoSavePhoto, folderName, saveImageResult);
        ambientLightManager.start(cameraManager);

        SurfaceHolder surfaceHolder = view.getSurfaceView().getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder, getDisplayRotation());
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    /**
     * 停止相机onPause()
     *
     * 必须在Activity的super.onPause()之前调用
     */
    public void onPause(){
        if(!hasSurface){
            view.getSurfaceView().getHolder().removeCallback(this);
        }
        if(ambientLightManager != null){
            ambientLightManager.stop();
        }
        if(cameraManager != null){
            cameraManager.stopPreview();
            cameraManager.closeDriver();
        }
    }

    /**
     * 销毁Activity，onDestroy()
     *
     * 在Activity的onDestroy()方法中调用
     */
    public void onDestroy(){
        cameraManager.release();
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
            handler.reference.clear();
        }
        onDetach();
        System.gc();
    }

    /**
     * 改变摄像头方向(前置/后置)
     */
    public void changeDirection() {
        facing = facing == CameraFacing.BACK ? CameraFacing.FRONT : CameraFacing.BACK;
        cameraManager.changeDirection(view.getSurfaceView().getHolder(), facing, getDisplayRotation());
        supportCameraScale(supportCameraPreviewScale);
    }

    /** 拍照(拍照成功后会生成bitmap存于内存中，如设置自动生成本地文件会保存文件) */
    public void takePhoto(){
        if(cameraManager != null){
            cameraManager.takePhoto(new CameraManager.PictureResult() {
                @Override
                public void onSuccess(Camera camera, Bitmap bitmap) {
                    view.onTakePhotoSuccess(bitmap);
                    camera.startPreview();
                }
            });
        }
    }

    /** 保存拍照获取的Bitmap到文件中去 */
    public void saveBitmap(final String folderName, final String fileName, final Bitmap bitmap){
        view.showProgress(true);
        ExecutorService singleThreadExecutor= Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable(){
            public void run(){
                FileUtils.saveBitmapFile(bitmap,
                        SAVE_PATH + "/" + folderName,
                        fileName.endsWith(".jpg")?fileName:fileName + ".jpg",
                        new FileUtils.SaveImageResult() {

                    @Override
                    public void onSaveSuccess(File imageFile, String fileName) {
                        Map<String, File> successMap = new HashMap<>();
                        successMap.put("file", imageFile);
                        Message msg = Message.obtain();
                        msg.what = SAVE_SUCCESS;
                        msg.obj = successMap;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onSaveFailed(String retFlag, String retMsg) {
                        Map<String, String> failedMap = new HashMap<>();
                        failedMap.put("retFlag", retFlag);
                        failedMap.put("retMsg", retMsg);
                        Message msg = Message.obtain();
                        msg.what = SAVE_FAILED;
                        msg.obj = failedMap;
                        handler.sendMessage(msg);
                    }
                });
            }
        });
        singleThreadExecutor.shutdown();
    }

    /**
     * 设置是否支持 预览界面及拍照 缩放
     *
     * 使用该功能需要使用定制的CameraScaleView替代surfaceView作为预览界面
     *
     * @param scale true为支持；false为不支持缩放
     */
    public void supportCameraScale(boolean scale){
        this.supportCameraPreviewScale = scale;
        try{
            if(view.getSurfaceView() instanceof CameraScaleView){
                if(scale){
                    ((CameraScaleView) view.getSurfaceView()).setCamera(cameraManager.getOpenCamera().getCamera());
                }else{
                    ((CameraScaleView) view.getSurfaceView()).setCamera(null);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 是否自动保存图片
     *
     * @param isAutoSave    若为自动保存，则是连拍模式
     */
    public void setAutoSavePhoto(boolean isAutoSave, String folderName, FileUtils.SaveImageResult result){
        isAutoSavePhoto = isAutoSave;
        if(!isAutoSave){
            folderName = null;
            result = null;
        }
        this.folderName = folderName;
        if(cameraManager != null){
            cameraManager.setAutoSavePhoto(isAutoSave, folderName, result);
        }
        saveImageResult = result;
    }

    public void setCustomSize(int width, int height){
        if(width <= 0 || height <= 0){
            return;
        }
        customHeight = height;
        customWidth = width;

        if(cameraManager != null){
            cameraManager.setCustomSize(width, height);
        }
    }

    //辅助方法，初始化相机
    private void initCamera(SurfaceHolder surfaceHolder, int degrees){
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder, facing, degrees);
            setCustomSize(customWidth, customHeight);
            cameraManager.startPreview();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        supportCameraScale(supportCameraPreviewScale);
    }

    //surfaceHolder的回调
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder, getDisplayRotation());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(hasSurface){
            view.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraManager.autoFocus();
                }
            });
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: return 0;
            case Surface.ROTATION_90: return 90;
            case Surface.ROTATION_180: return 180;
            case Surface.ROTATION_270: return 270;
        }
        return 0;
    }

    private static final class CameraHandler extends Handler{

        private final WeakReference<CustomCameraPresenter> reference;
        public CameraHandler(CustomCameraPresenter presenter) {
            reference = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            reference.get().view.showProgress(false);
            int what = msg.what;
            switch (what) {
                case SAVE_SUCCESS:
                    Map<String, File> successMap = (Map<String, File>) msg.obj;
                    File imageFile = successMap.get("file");
                    if(FileUtils.exists(imageFile)){
                        reference.get().view.onSaveBitmapSuccess(imageFile);
                    }else{
                        reference.get().toast("图片保存失败");
                        reference.get().view.onSaveBitmapFailed("图片保存失败");
                    }
                    break;
                case SAVE_FAILED:
                    Map<String, String> failedMap = (Map<String, String>) msg.obj;
                    reference.get().toast(failedMap.get("retMsg") + "");
                    reference.get().view.onSaveBitmapFailed(failedMap.get("retMsg") + "");
                    break;
            }
        }
    }
}
