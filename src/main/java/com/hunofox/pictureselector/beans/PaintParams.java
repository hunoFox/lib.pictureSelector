package com.hunofox.pictureselector.beans;

import android.graphics.Color;
import androidx.annotation.ColorInt;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/8/16 10:05.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class PaintParams {

    public PaintParams() {}

    public PaintParams(@ColorInt int color, float stroke, boolean isDrawOutSide, boolean scaleEnable) {
        this.color = color;
        this.stroke = stroke;
        this.isDrawOutSide = isDrawOutSide;
        this.scaleEnable = scaleEnable;
    }

    public int color = Color.RED;//画笔颜色
    public float stroke = 10.0f;//画笔粗细
    public boolean isDrawOutSide = false;//默认不能画到界外
    public boolean scaleEnable = true;//默认开启缩放功能
    public boolean isRecycleBitmap = true;//默认回收bitmap

    private String saveFloderName;//保存文件夹名称
    private String saveFileName;//保存文件名称

    public String getSaveFloderName() {
        return saveFloderName;
    }

    public void setSaveFloderName(String saveFloderName) {
        if(saveFloderName == null){
            this.saveFloderName = saveFloderName;
        }else{
            this.saveFloderName = saveFloderName.startsWith("/")?saveFloderName:"/"+saveFloderName;
        }
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public void setSaveFileName(String saveFileName) {
        if(saveFileName == null){
            this.saveFileName = saveFileName;
        }else{
            this.saveFileName = saveFileName.startsWith("/")?saveFileName:"/"+saveFileName;
            this.saveFileName = saveFileName.endsWith(".jpg")?saveFileName:saveFileName + ".jpg";
        }
    }
}
