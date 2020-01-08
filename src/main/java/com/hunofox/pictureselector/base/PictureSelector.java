package com.hunofox.pictureselector.base;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.model.LazyHeaders;

/**
 * 项目名称：MyLibApp
 * 项目作者：胡玉君
 * 创建日期：2017/9/19 11:05.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public class PictureSelector extends Application{

    public static Context CONTEXT;

    public static String SAVE_PATH = "";
    public static String ZIP_PATH = "ZIP";

    public static final int MODE_SINGLE = 184;
    public static final int MODE_MULTI = 984;

    public static LazyHeaders.Builder GLIDE_BUILDER = null;

    @Override
    public void onCreate() {
        super.onCreate();

        CONTEXT = getApplicationContext();
    }

    /**
     * 若不继承该Application，则必须在自定义的Application中初始化一下参数
     *
     * @param context   不能为null
     */
    public static void init(@NonNull Context context){
        CONTEXT = context.getApplicationContext();

        SAVE_PATH = CONTEXT.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/";
        ZIP_PATH = CONTEXT.getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/ZIP").getAbsolutePath() + "/";
    }

    public static void setSavePath(String folder){
        setSavePath(folder, false);
    }
    public static void setSavePath(String folder, boolean absolutePath){
        if(!TextUtils.isEmpty(folder)){
            String path = folder.endsWith("/")?folder:(folder+"/");
            if(absolutePath){
                SAVE_PATH = path;
                ZIP_PATH = path + "ZIP/";
            }else{
                SAVE_PATH = CONTEXT.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + path;
                ZIP_PATH = SAVE_PATH + "ZIP/";
            }
        }
    }
}
