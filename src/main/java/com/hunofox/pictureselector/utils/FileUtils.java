package com.hunofox.pictureselector.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


import com.hunofox.pictureselector.base.PictureSelector;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static android.os.Environment.MEDIA_MOUNTED;
import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;
import static com.hunofox.pictureselector.base.PictureSelector.SAVE_PATH;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/21 9:33.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class FileUtils {

    /**
     * 创建一个临时文件
     *
     * @param folderName 文件所在文件夹名称
     * @param fileName 文件名称(不要带文件扩展类型)
     * @param extension 文件扩展名
     */
    public static File createTempFile(String folderName, String fileName, String extension) throws IOException {
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File temp = new File(SAVE_PATH + folderName);
            if (!exists(temp)) {
                if(!temp.mkdirs()){
                    return null;
                }
            }
            String filePath = SAVE_PATH + folderName + "/" + fileName + "_" + System.currentTimeMillis();
            if(extension != null && !TextUtils.isEmpty(extension.trim())){
                filePath = filePath + "." + extension;
            }
            File result = new File(filePath);
            if(result.createNewFile()){
                return result;
            }
        }
        return null;
    }

    /**
     * 获取文件夹下文件列表
     *
     * @param folderName 文件夹
     * @param extension 指定扩展名
     * @return 文件路径(返回结果不为null, 但有可能为空集合)
     */
    public static List<String> getFileList(String folderName, String extension){
        List<String> list = new ArrayList<>();
        File file = new File(SAVE_PATH + folderName);
        if(exists(file)){
            String [] paths = file.list();
            if(paths != null && paths.length > 0){
                for(String str:paths){
                    str = SAVE_PATH + folderName + "/" +str;
                    if(extension != null && !TextUtils.isEmpty(extension.trim()) && str.endsWith(extension)){
                        list.add(str);
                    }else if(extension == null || TextUtils.isEmpty(extension.trim())){
                        list.add(str);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 保存一个Bitmap到本地文件
     * <p>
     * 注意：
     * 1. 文件存储路径不能乱存，如何存储请看AppApplication
     * 2. 调用前请检查SD卡写入权限
     * 3. 保存图片建议在IO线程中执行
     *
     * @param bitmap   图像
     * @param filePath 保存路径，可以不以“/”结尾，需要写全部路径，init中设置的路径没有在这里面写出来
     * @param fileName 文件名，可以不以“.jpg”结尾
     * @param result   存储回调，可以为null
     */
    public static void saveBitmapFile(Bitmap bitmap, String filePath, String fileName, SaveImageResult result) {
        if (bitmap == null) {
            if (result != null) {
                result.onSaveFailed("HUNO_NULL", "没有获取到图像信息，保存失败");
            }
            return;
        }
        boolean sdCardWritePermission =
                CONTEXT.getPackageManager().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, CONTEXT.getPackageName()) == PackageManager.PERMISSION_GRANTED;
        if (!sdCardWritePermission) {
            if (result != null) {
                result.onSaveFailed("HUNO_PERMISSION", "没有sd卡读写权限");
            }
            return;
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(filePath);
            if (!exists(file)) {
                file.mkdirs();
            }
            File imageFile = new File(file.getPath() + "/" + fileName);
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                //todo
                CONTEXT.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
                if (result != null) {
                    result.onSaveSuccess(imageFile, fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (result != null) {
                    result.onSaveFailed("HUNO_ERROR", e.getMessage());
                }
            } finally {
                if (outStream != null) {
                    try {
                        outStream.flush();
                        outStream.close();
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            if (result != null) {
                result.onSaveFailed("HUNO_SDCARD_INVALIBLE", "sd卡不可用");
            }
        }
    }

    /**
     * 保存一个byte[]到本地文件
     * <p>
     * 注意：
     * 1. 文件存储路径不能乱存，如何存储请看AppApplication
     * 2. 调用前请检查SD卡写入权限
     * 3. 保存图片建议在IO线程中执行
     *
     * @param data     原始数据
     * @param filePath 保存路径，可以不以“/”结尾
     * @param fileName 文件名，可以不以“.jpg”结尾
     * @param result   存储回调，可以为null
     */
    public static void saveByteDataFile(byte[] data, String filePath, String fileName, SaveImageResult result) {
        if (data == null) {
            if (result != null) {
                result.onSaveFailed("HUNO_NULL", "没有获取到数据信息，保存失败");
            }
            return;
        }
        boolean sdCardWritePermission =
                CONTEXT.getPackageManager().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, CONTEXT.getPackageName()) == PackageManager.PERMISSION_GRANTED;
        if (!sdCardWritePermission) {
            if (result != null) {
                result.onSaveFailed("HUNO_PERMISSION", "没有sd卡读写权限");
            }
            return;
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(filePath);
            if (!exists(file)) {
                file.mkdirs();
            }
            File imageFile = new File(file.getPath() + "/" + fileName);
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(imageFile);
                outStream.write(data);
                CONTEXT.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
                if (result != null) {
                    result.onSaveSuccess(imageFile, fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (result != null) {
                    result.onSaveFailed("HUNO_ERROR", e.getMessage());
                }
            } finally {
                if (outStream != null) {
                    try {
                        outStream.flush();
                        outStream.close();
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            if (result != null) {
                result.onSaveFailed("HUNO_SDCARD_INVALIBLE", "sd卡不可用");
            }
        }
    }

    public interface SaveImageResult{
        void onSaveSuccess(File imageFile, String fileName);
        void onSaveFailed(String retFlag, String retMsg);
    }

    /**
     * 将字节写入文件
     *
     * @param file 要写入的文件
     * @param bytes 字节输出
     * @throws IOException IO异常
     */
    public static void writeByteArrayToFile(File file, byte[] bytes) throws IOException{
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        outputStream.close();
    }

    /**
     * 根据文件路径判断文件是否存在(适配AndroidQ)
     *
     * @param path 文件路径，可以为空
     * @return true存在；false不存在
     */
    public static boolean exists(String path){
        if(path == null || path.trim().length() <= 0){
            return false;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            //适配Android 10
            AssetFileDescriptor afd = null;
            ContentResolver cr = CONTEXT.getContentResolver();
            try {
                Uri uri = Uri.parse(path);
                afd = cr.openAssetFileDescriptor(uri, "r");
                if (afd == null) {
                    return false;
                } else {
                    close(afd);
                }
            } catch (FileNotFoundException e) {
                return false;
            }finally {
                close(afd);
            }
            return true;
        }else{
            return new File(path).exists();
        }
    }
    public static boolean exists(File file){
        if(file == null){
            return false;
        }
        return exists(file.getAbsolutePath());
    }
    private static void close(AssetFileDescriptor afd){
        try{
            afd.close();
        }catch (Exception e){}
    }

    /**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && exists(file)) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!exists(dirFile) || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     * @param filePath  要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!exists(file)) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * 获取图片文件宽高
     *
     * @param path  文件路径
     * @return  0宽；1高
     */
    public static int[] getBitmapFileSize(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//这个参数设置为true才有效，
        BitmapFactory.decodeFile(path, options);//这里的bitmap是个空
        int outHeight=options.outHeight;
        int outWidth= options.outWidth;
        int[] size = new int[2];
        size[0] = outWidth;
        size[1] = outHeight;
        return size;
    }

    /**
     * 修正宽高，保证宽高不超出手机
     *
     * @param picWidth      图片原始宽度
     * @param picHeight     图片原始高度
     * @return              0修正后宽度；1修正后高度
     */
    public static int[] fixPictureSize(int picWidth, int picHeight){
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) PictureSelector.CONTEXT.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int newWidth = picWidth;
        int newHight = picHeight;
        if(screenWidth != picWidth){
            newWidth = screenWidth;
            newHight = (int)((float)screenWidth/ ((float) picWidth/(float) picHeight));
        }

        int[] fix = new int[2];
        fix[0] = newWidth;
        fix[1] = newHight;
        return fix;
    }

    public static int[] fixPictureSize(String imagePath){
        int[] org = getBitmapFileSize(imagePath);
        return fixPictureSize(org[0], org[1]);
    }
}
