/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hunofox.pictureselector.widget.customCamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.customCamera.open.CameraFacing;
import com.hunofox.pictureselector.widget.customCamera.open.OpenCamera;
import com.hunofox.pictureselector.widget.customCamera.open.OpenCameraInterface;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;
import static com.hunofox.pictureselector.base.PictureSelector.SAVE_PATH;


/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
@SuppressWarnings("deprecation") // camera APIs
public class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

    private final Context context;
    private final CameraConfigurationManager configManager;
    private OpenCamera camera;
    private AutoFocusManager autoFocusManager;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private int requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;

    private boolean isAutoSavePhoto = false;
    private String folderName = "camera/";
    private FileUtils.SaveImageResult saveResult;

    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private final PreviewCallback previewCallback;

    public CameraManager(Context context) {
        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
        previewCallback = new PreviewCallback(configManager);
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder  The surface object which the camera will draw preview frames into.
     * @param degrees activity默认的旋转的方向
     * @throws IOException Indicates the camera driver failed to open.
     */
    public synchronized void openDriver(SurfaceHolder holder, CameraFacing facing, int degrees) throws IOException {
        OpenCamera theCamera = camera;
        if (theCamera == null) {
            theCamera = OpenCameraInterface.open(requestedCameraId, facing, degrees);
            if (theCamera == null) {
                throw new IOException("Camera.open() failed to return object from driver");
            }
            camera = theCamera;
        }

        if (!initialized) {
            initialized = true;
            configManager.initFromCameraParameters(theCamera);
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
                requestedFramingRectWidth = 0;
                requestedFramingRectHeight = 0;
            }
        }

        Camera cameraObject = theCamera.getCamera();
        Camera.Parameters parameters = cameraObject.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
        try {
            configManager.setDesiredCameraParameters(theCamera, false);
        } catch (RuntimeException re) {
            // Driver failed
            Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
            Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
            // Reset:
            if (parametersFlattened != null) {
                parameters = cameraObject.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    cameraObject.setParameters(parameters);
                    configManager.setDesiredCameraParameters(theCamera, true);
                } catch (RuntimeException re2) {
                    // Well, darn. Give up
                    Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }
        cameraObject.setPreviewDisplay(holder);
    }

    public void setCustomSize(int customWidth, int customHeight){
        if(camera != null && camera.getCamera() != null){
            Camera.Parameters parameters = camera.getCamera().getParameters();
            if(parameters != null){
                Camera.Size preSize = OpenCamera.getCloselyPreSize(true, customWidth, customHeight, parameters.getSupportedPreviewSizes());
                if(preSize != null){
                    parameters.setPreviewSize(preSize.width, preSize.height);
                    camera.getCamera().setParameters(parameters);
                }
            }
        }
    }

    //拍照
    private Camera.Size reSize = null;
    public void takePhoto(PictureResult result) {
        this.result = result;
        if (camera != null) {
            Camera.Parameters parameters = camera.getCamera().getParameters();
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();

            int picX = parameters.getPreviewSize().width;
            int picY = parameters.getPreviewSize().height;

            //按屏幕大小截取图片
            WindowManager wm = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
            Point point = new Point();
            wm.getDefaultDisplay().getSize(point);
            picX = pictureSizes.get(0).width;
            picY = pictureSizes.get(0).height;
            boolean isResize = false;
            for(Camera.Size size:pictureSizes){
                if(size.width == point.y && size.height == point.x){
                    picX = size.width;
                    picY = size.height;
                    isResize = true;
                    break;
                }
            }

            if(!isResize){
                //按预览大小截图图片
                float rot = (float)picY/(float)picX;
                reSize = null;
                float close = Float.MAX_VALUE;
                for(Camera.Size size:pictureSizes){
                    float rerot = (float)size.height/(float)size.width;
                    if(Math.abs(close) > Math.abs((rerot - rot))){
                        close = rerot - rot;
                        reSize = size;
                    }else if(Math.abs(close) == Math.abs((rerot - rot))){
                        if(size.width > reSize.width && size.height > reSize.height){
                            close = rerot - rot;
                            reSize = size;
                        }
                    }
                }

                if(reSize != null){
                    picX = reSize.width;
                    picY = reSize.height;
                }
            }

            parameters.setPictureSize(picX, picY);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.set("orientation", "portrait");
            parameters.set("rotation", CameraManager.this.camera.getOrientation());
            camera.getCamera().setParameters(parameters);
            camera.getCamera().takePicture(null, null, jpegCallBack);
        }
    }

    //创建jpeg图片回调数据对象
    private PictureResult result;
    private Camera.PictureCallback jpegCallBack = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, final Camera camera) {

            if(isAutoSavePhoto){
                ExecutorService service = Executors.newFixedThreadPool(1);
                service.execute(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.saveByteDataFile(data, SAVE_PATH+folderName, System.currentTimeMillis()+".jpg", saveResult);
                    }
                });
                service.shutdown();
            }

            try {
                Bitmap bitmap;

                //按预览大小截图图片
                Camera.Parameters parameters = camera.getParameters();
                int picX = parameters.getPreviewSize().width;
                int picY = parameters.getPreviewSize().height;

                int sampleSize = 0;
                if(reSize != null){
                    sampleSize = (int)(Math.sqrt((double)reSize.height/(double) picY) + 0.5);
                }

                if(sampleSize > 1 && reSize.width > picX && reSize.height > picY){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inSampleSize = sampleSize;
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                }else{
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }

                if(CameraManager.this.camera.getFacing() == CameraFacing.FRONT){
                    //前置摄像头拍照后会默认水平镜像，这里的操作是再次镜像回来
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    Matrix matrix = new Matrix();
                    matrix.postScale(-1, 1);

                    Bitmap convertBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
                    bitmap.recycle();
                    bitmap = null;
                    System.gc();
                    bitmap = convertBitmap;
                }
                camera.stopPreview();//关闭预览 处理数据
                if (result != null) {
                    result.onSuccess(camera, bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reSize = null;
            }
        }
    };

    public interface PictureResult {
        /**
         * 成功后返回camera和bitmap
         * <p>
         * 1. 需要调用camera.startPreview方法，在处理结束后重新连续取景
         * <p>
         * 2. 需要将bitmap用完后回收bitmap.recycle();
         */
        void onSuccess(Camera camera, Bitmap bitmap);
    }

    //改变摄像头方向
    public synchronized void changeDirection(SurfaceHolder holder, CameraFacing facing, int degrees) {
        Camera.Size size = null;
        if (camera != null) {
            size = camera.getCamera().getParameters().getPreviewSize();
            camera.getCamera().stopPreview();
            camera.getCamera().release();
            camera = null;
        }
        initialized = false;
        try {
            openDriver(holder, facing, degrees);
            if(size != null){
                setCustomSize(size.height, size.width);
            }
            camera.getCamera().startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (camera != null) {
            camera.getCamera().release();
            camera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null;
            framingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        OpenCamera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.getCamera().startPreview();
            previewing = true;
            autoFocusManager = new AutoFocusManager(context, theCamera.getCamera());
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if (camera != null && previewing) {
            camera.getCamera().stopPreview();
            previewCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    public synchronized void autoFocus() {
        if (autoFocusManager == null) {
            autoFocusManager = new AutoFocusManager(CONTEXT, camera.getCamera());
        }
        autoFocusManager.start();
    }

    public synchronized void setTorch(boolean newSetting) {
        OpenCamera theCamera = camera;
        if (theCamera != null && newSetting != configManager.getTorchState(theCamera.getCamera())) {
            boolean wasAutoFocusManager = autoFocusManager != null;
            if (wasAutoFocusManager) {
                autoFocusManager.stop();
                autoFocusManager = null;
            }
            configManager.setTorch(theCamera.getCamera(), newSetting);
            if (wasAutoFocusManager) {
                autoFocusManager = new AutoFocusManager(context, theCamera.getCamera());
                autoFocusManager.start();
            }
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public synchronized void requestPreviewFrame(Handler handler, int message) {
        OpenCamera theCamera = camera;
        if (theCamera != null && previewing) {
            previewCallback.setHandler(handler, message);
            theCamera.getCamera().setOneShotPreviewCallback(previewCallback);
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public synchronized Rect getFramingRect() {
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            Point screenResolution = configManager.getScreenResolution();
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    public synchronized Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }


    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     */
    public synchronized void setManualCameraId(int cameraId) {
        requestedCameraId = cameraId;
    }

    /**
     * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
     * them automatically based on screen resolution.
     *
     * @param width  The width in pixels to scan.
     * @param height The height in pixels to scan.
     */
    public synchronized void setManualFramingRect(int width, int height) {
        if (initialized) {
            Point screenResolution = configManager.getScreenResolution();
            if (width > screenResolution.x) {
                width = screenResolution.x;
            }
            if (height > screenResolution.y) {
                height = screenResolution.y;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated manual framing rect: " + framingRect);
            framingRectInPreview = null;
        } else {
            requestedFramingRectWidth = width;
            requestedFramingRectHeight = height;
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    public OpenCamera getOpenCamera(){
        return camera;
    }

    public boolean isAutoSavePhoto() {
        return isAutoSavePhoto;
    }

    public void setAutoSavePhoto(boolean autoSavePhoto, String folderName, FileUtils.SaveImageResult saveResult) {
        isAutoSavePhoto = autoSavePhoto;
        if(folderName != null && folderName.length() > 0){
            this.folderName = folderName.endsWith("/")?folderName:folderName+"/";
        }
        this.saveResult = saveResult;
    }

    public void release(){
        try{
            camera.getCamera().release();
        }catch (Exception e){}
    }


}
