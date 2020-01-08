package com.hunofox.pictureselector.base;


import android.widget.Toast;

import java.lang.ref.WeakReference;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;


/**
 * 项目名称：
 * 项目作者：胡玉君
 * 创建日期：2017/6/3 9:55.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class PictureSelectorPresenter<T extends PictureSelectView>{

    private WeakReference<T> reference;
    protected T view;
    public PictureSelectorPresenter(T view) {
        onAttach(view);
    }

    public void onAttach(T view){
        if(!isAttached()){
            this.reference = new WeakReference<>(view);
            this.view = reference.get();
        }
    }
    public boolean isAttached(){
        return reference != null && reference.get() != null;
    }

    public void onDetach(){
        if(reference != null){
            reference.clear();
        }
        reference = null;
        view = null;
    }

    /**
     * 单例Toast，测试用
     */
    private static Toast mToast;
    public void toast(CharSequence msg) {
        if (mToast == null) {
            mToast = Toast.makeText(CONTEXT, "", Toast.LENGTH_LONG);
        }
        mToast.setText(msg);
        mToast.show();
    }
}
