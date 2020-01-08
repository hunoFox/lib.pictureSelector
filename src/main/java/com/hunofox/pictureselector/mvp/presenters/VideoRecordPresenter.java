package com.hunofox.pictureselector.mvp.presenters;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;

import com.hunofox.pictureselector.base.PictureSelector;
import com.hunofox.pictureselector.base.PictureSelectorPresenter;
import com.hunofox.pictureselector.mvp.views.IVideoView;
import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.customCamera.AmbientLightManager;
import com.hunofox.pictureselector.widget.customCamera.CameraManager;
import com.hunofox.pictureselector.widget.customCamera.open.CameraFacing;
import com.hunofox.pictureselector.widget.customCamera.open.CameraScaleView;
import com.hunofox.pictureselector.widget.customCamera.open.OpenCamera;

import java.io.File;
import java.util.List;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/10/18 13:23.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 *
 * 需要权限：相机权限、sd卡读写权限、音频录制权限
 * ----------------------------------------------------------------------------------------------------
 */
public class VideoRecordPresenter extends PictureSelectorPresenter<IVideoView> implements SurfaceHolder.Callback {

    private CameraManager cameraManager;
    private final AmbientLightManager ambientLightManager;
    private boolean hasSurface = false;
    private CameraFacing facing = CameraFacing.BACK;
    private boolean supportCameraPreviewScale = false;
    private boolean isOnPause = false;

    private MediaRecorder mr;
    private Camera.Size reSize;

    private int customWidth = -1;
    private int customHeight = -1;

    private String filePath = null;
    private int state = 0;

    private RecordListener recordListener;
    public interface RecordListener{
        int STATE_FREE = 0;
        int STATE_RECORDING = 1;
        int STATE_PAUSE = 2;

        void onStartRecord();
        void onPauseRecord();
        void onStopRecord(String filePath);
        void onRestartRecord();
    }

    public VideoRecordPresenter(IVideoView view) {
        super(view);

        ambientLightManager = new AmbientLightManager(CONTEXT);
    }

    public int getCurrentState(){
        return state;
    }

    public void setRecordListener(RecordListener recordListener) {
        this.recordListener = recordListener;
    }

    /**
     * 开启相机onResume()
     *
     * 该方法只能在onResume()中调用
     */
    public void onResume(){
        isOnPause = false;
        if(cameraManager == null){
            cameraManager = new CameraManager(CONTEXT);
        }

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
        isOnPause = true;
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
        try{
            mr.reset();
            mr.release();
            mr = null;
        }catch (Exception e){
            e.printStackTrace();
        }

        onDetach();
        System.gc();
    }

    public void setCustomSize(int width, int height){
        if(width < 0 || height < 0){
            return;
        }
        customHeight = height;
        customWidth = width;

        if(cameraManager != null){
            cameraManager.setCustomSize(width, height);
        }
    }

    /**
     * 改变摄像头方向(前置/后置)
     */
    public void changeDirection() {
        facing = facing == CameraFacing.BACK ? CameraFacing.FRONT : CameraFacing.BACK;
        cameraManager.changeDirection(view.getSurfaceView().getHolder(), facing, getDisplayRotation());
        supportCameraScale(supportCameraPreviewScale);
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

    /**
     * 开始录像
     *
     * @param fileName  录像文件保存文件夹
     */
    public void recordVideo(String fileName, String folderName){
        if(isOnPause){
            onResume();
        }
        state = RecordListener.STATE_FREE;
        filePath = null;
        if(cameraManager!= null){
            releaseMR();
            mr = new MediaRecorder();
            OpenCamera openCamera = cameraManager.getOpenCamera();
            mr.setCamera(openCamera.getCamera());
            openCamera.getCamera().unlock();

            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            mr.setOrientationHint(90);
            mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mr.setOutputFormat(profile.fileFormat);
            mr.setAudioEncoder(profile.audioCodec);
            mr.setVideoEncoder(profile.videoCodec);
            mr.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            mr.setVideoFrameRate(profile.videoFrameRate);
            mr.setVideoEncodingBitRate(profile.videoBitRate);
            mr.setAudioEncodingBitRate(profile.audioBitRate);
            mr.setAudioChannels(profile.audioChannels);
            mr.setAudioSamplingRate(profile.audioSampleRate);
            mr.setMaxDuration(60000);//最长录制时间1分钟
            mr.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if(MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what){
                        stopRecord();
                    }
                }
            });

            //配置视频输出路径
            folderName = (folderName == null || folderName.trim().length() <= 0)?"hunoFoxVideo":folderName;
            File file = new File(PictureSelector.SAVE_PATH + "/" + folderName);
            if (!FileUtils.exists(file)) {
                file.mkdirs();
            }
            fileName = fileName.contains(".")?fileName:fileName+".mp4";
            filePath = file.getPath() + "/" + fileName;
            mr.setOutputFile(filePath);
            mr.setPreviewDisplay(view.getSurfaceView().getHolder().getSurface());

            try{
                mr.prepare();
                mr.start();
                state = RecordListener.STATE_RECORDING;
            }catch (Exception e){
                e.printStackTrace();
            }

            if(state == RecordListener.STATE_RECORDING){
                if(recordListener != null){
                    recordListener.onStartRecord();
                }
            }
        }
    }

    /**
     * 暂停录制
     */
    public void pause(){
        try{
            if(mr != null){
                //Android7.0以上才能使用视频录制暂停
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mr.pause();
                    state = RecordListener.STATE_PAUSE;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(state == RecordListener.STATE_PAUSE){
            if(recordListener != null){
                recordListener.onPauseRecord();
            }
        }
    }

    /**
     * 恢复录制
     */
    public void resume(){
        boolean flag = state == RecordListener.STATE_PAUSE;
        try{
            if(mr != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mr.resume();
                    state = RecordListener.STATE_RECORDING;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(state == RecordListener.STATE_RECORDING){
            if(recordListener != null){
                if(flag){
                    recordListener.onRestartRecord();
                }else{
                    recordListener.onStartRecord();
                }
            }
        }
    }

    /**
     * 停止录像
     */
    public void stopRecord(){
        releaseMR();
        if(recordListener != null){
            recordListener.onStopRecord(filePath);
        }
    }

    public void releaseMR(){
        try{
            if(mr != null){
                mr.stop();
                mr.reset();
                mr.release();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            state = RecordListener.STATE_FREE;
        }
        mr = null;
    }


}
