package com.hunofox.pictureselector.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.adaptor.FolderSelectAdaptor;
import com.hunofox.pictureselector.beans.FolderBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/28 16:50.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 *
 * 未完成：
 *
 * 1. 选择后勾选框不会立即变为当前勾选的内容
 * ----------------------------------------------------------------------------------------------------
 */
public class FolderSelectedPopupWindow extends PopupWindow{

    private final List<FolderBean> datas;
    private final FolderSelectAdaptor adaptor;
    private final WeakReference<Context> reference;

    private View view;
    private Animation startAnim;
    private Animation dismissAnim;

    public FolderSelectedPopupWindow(Context context, final List<FolderBean> datas) {
        super(context);
        reference = new WeakReference<>(context);

        //设置数据
        if(datas == null){
            this.datas = new ArrayList<>();
        }else{
            this.datas = datas;
        }

        //设置View
        view = View.inflate(context, R.layout.popup_folder_selector, null);
        this.setContentView(view);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        adaptor = new FolderSelectAdaptor(this.datas);
        recyclerView.setAdapter(adaptor);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //设置宽高
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        this.setWidth(size.x);
        this.setHeight((int) (size.y * (5.5f/8.0f)));

        //设置动画效果
        dismissAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1);
        dismissAnim.setDuration(250);
        dismissAnim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                FolderSelectedPopupWindow.super.dismiss();
            }
            public void onAnimationRepeat(Animation animation) {}
        });
        startAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0
        );
        startAnim.setDuration(250);

        //设置点击空白区域消失
        setFocusable(true);
        setOutsideTouchable(true);
    }

    //展示popupWindow
    public void show(final View anchor) {
        setBackgroundAlpha(0.5f);
        showAtLocation(anchor,
                Gravity.START + Gravity.BOTTOM,
                0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, anchor.getContext().getApplicationContext().getResources().getDisplayMetrics()));
        view.startAnimation(startAnim);
    }

    //popupWindow消失
    public void dismiss() {
        setBackgroundAlpha(1.0f);
        view.startAnimation(dismissAnim);
    }

    //通知数据发生了改变
    public void notifyDataSetChanged(List<FolderBean> datas){
        this.datas.clear();
        this.datas.addAll(datas);
        adaptor.notifyDataSetChanged();
    }

    //点击事件
    public void setOnSelectListener(final OnSelectListener listener){
        adaptor.setOnItemClickListener(new FolderSelectAdaptor.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(listener != null){
                    listener.onSelectListener(datas.get(position));
                }
            }
        });
    }
    public interface OnSelectListener{
        void onSelectListener(FolderBean folder);
    }

    //自定义展示、消失的动画
    public void setAnimation(Animation startAnim, Animation dismissAnim){
        this.startAnim = startAnim;
        this.dismissAnim = dismissAnim;
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     * 屏幕透明度0.0-1.0 1表示完全不透明
     */
    public void setBackgroundAlpha(float bgAlpha) {
        if(reference.get() != null && reference.get() instanceof Activity){
            Activity activity = (Activity) reference.get();
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.alpha = bgAlpha;
            activity.getWindow().setAttributes(lp);
        }
    }

    /**
     * 释放资源
     */
    public void release(){
        this.datas.clear();
        reference.clear();
    }
}
