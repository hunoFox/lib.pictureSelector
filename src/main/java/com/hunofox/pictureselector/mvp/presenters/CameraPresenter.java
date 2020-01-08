package com.hunofox.pictureselector.mvp.presenters;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import com.hunofox.pictureselector.base.PictureSelectorPresenter;
import com.hunofox.pictureselector.mvp.views.ICameraView;
import com.hunofox.pictureselector.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/21 9:27.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 *
 * 适配7.0
 *
 * 第一步：在Manifest中配置provider
 * <provider
 *  android:name="android.support.v4.content.FileProvider"
 *  android:authorities="{包名}.fileProvider"
 *  android:exported="false"
 *  android:grantUriPermissions="true">
 *      <meta-data
 *          android:name="android.support.FILE_PROVIDER_PATHS"
 *          android:resource="@xml/file_paths"/>
 * </provider>
 *
 * 第二步：在res下创建xml包，在xml中创建file_paths.xml(可以使用该类库中的xml)
 * <paths>
 *  <external-path path="Android/data/{包名}/" name="files_root" />
 *  <external-path path="." name="external_storage_root" />
 * </paths>
 *
 * 说明：
 * 1. 该presenter会引导用户手动开启相机权限 和 读写sd卡权限
 * 2. 该presenter提供了两种简单的文件压缩途径,也可以使用第三方库LuBan压缩图片
 *
 * ----------------------------------------------------------------------------------------------------
 */
public class CameraPresenter extends PictureSelectorPresenter<ICameraView> {

    //相机拍照后存放的临时文件
    private File cameraFile;

    public CameraPresenter(ICameraView view) {
        super(view);
    }

    /**
     * 第一步：开启相机
     *
     * 注意：调用前需要检查两项权限：相机权限和读写sd卡权限
     *
     * @param folderName 文件所在文件夹名称
     * @param fileName   文件名称
     */
    public void openCamera(String folderName, String fileName) {
        cameraFile = null;
        Intent intent = new Intent();

        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(CONTEXT.getPackageManager()) != null && CONTEXT.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            try {
                cameraFile = FileUtils.createTempFile(folderName, fileName, "jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }

            String packageName;
            try {
                packageName = CONTEXT.getPackageManager().getPackageInfo(CONTEXT.getPackageName(), 0).packageName;
            } catch (Exception e) {
                e.printStackTrace();
                cameraFile = null;
                toast("系统相机不可用");
                return;
            }

            if (FileUtils.exists(cameraFile)) {
                //适配7.0(记住：不要调用intent.setDataAndType())
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(CONTEXT,
                        packageName + ".fileProvider",
                        cameraFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                try {
                    view.startCameraActivityForResult(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    toast("系统相机不可用");
                }
            } else {
                toast("SD卡不可用");
            }
        } else {
            toast("系统相机不可用");
        }
    }


    /**
     * 第二步：处理返回结果
     *
     * 该方法需要在onActivityResult中调用才能正确处理结果
     */
    public void handleResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK && cameraFile != null) {
            //通知系统拍照完成
            CONTEXT.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cameraFile)));
            view.cameraResult(cameraFile.getAbsolutePath());
        } else {
            //删除临时文件
            while (FileUtils.exists(cameraFile)) {
                boolean success = cameraFile.delete();
                if (success) {
                    cameraFile = null;
                }
            }
        }
    }

    @Override
    public void onDetach() {
        cameraFile = null;
        super.onDetach();
    }
}
