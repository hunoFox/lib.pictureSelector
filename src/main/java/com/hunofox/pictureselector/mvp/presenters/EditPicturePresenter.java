package com.hunofox.pictureselector.mvp.presenters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.base.PictureSelector;
import com.hunofox.pictureselector.base.PictureSelectorPresenter;
import com.hunofox.pictureselector.beans.PaintParams;
import com.hunofox.pictureselector.mvp.views.IEditPictureView;
import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.pictureEditWeidgt.GraffitiListener;
import com.hunofox.pictureselector.widget.pictureEditWeidgt.GraffitiSelectableItem;
import com.hunofox.pictureselector.widget.pictureEditWeidgt.GraffitiText;
import com.hunofox.pictureselector.widget.pictureEditWeidgt.GraffitiView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;
import static com.hunofox.pictureselector.base.PictureSelector.SAVE_PATH;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/28 15:54.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public class EditPicturePresenter extends PictureSelectorPresenter<IEditPictureView> implements View.OnTouchListener {

    private static final float MAX_SCALE = 3f;
    private static final float MIN_SCALE = 1f;

    private GraffitiView graffitiView;
    private final SaveBitmapHandler handler;
    private Bitmap canvasBitmap;

    private Bitmap savedbitmap;

    //图片缩放相关内容
    private int mTouchMode = 0;
    private float mTouchLastX;
    private float mTouchLastY;
    private float mTouchCentreX;
    private float mTouchCentreY;
    private float mToucheCentreXOnGraffiti;
    private float mToucheCentreYOnGraffiti;
    private float mOldDist;
    private float mOldScale;
    private float mTouchSlop;
    private boolean mIsBusy = false;
    private boolean isScaling = false;

    public EditPicturePresenter(IEditPictureView view) {
        super(view);

        handler = new SaveBitmapHandler(this);
    }

    /**
     * 创建一个展示Bitmap的View至父布局中(该方法必须被调用，否则其他方法不能被执行)
     *
     * @param bitmap    需要展示的bitmap
     * @param container 父布局
     * @param param     初始化参数，可为null
     */
    public void showImage(Bitmap bitmap, ViewGroup container, final PaintParams param) {
        if (canvasBitmap != null) {
            canvasBitmap.recycle();
        }
        canvasBitmap = null;
        System.gc();
        this.canvasBitmap = bitmap;
        graffitiView = new GraffitiView(container.getContext(), canvasBitmap, new GraffitiListener() {

            @Override
            public void onSaved(Bitmap bitmap, Bitmap bitmapEraser) {
                view.showProgress(true);
                savedbitmap = bitmap;
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.execute(new Runnable() {
                    @Override
                    public void run() {
                        String floderName = param.getSaveFloderName();
                        floderName = (floderName == null || floderName.trim().length() <= 0) ? "/PictureMerge" : floderName;
                        String fileName = param.getSaveFileName();
                        fileName = (fileName == null || fileName.trim().length() <= 0) ? SystemClock.currentThreadTimeMillis() + ".jpg" : fileName;
                        FileUtils.saveBitmapFile(savedbitmap, SAVE_PATH + floderName, fileName, new FileUtils.SaveImageResult() {
                            @Override
                            public void onSaveSuccess(File imageFile, String fileName) {
                                if(param.isRecycleBitmap){
                                    releaseBitmap();
                                }

                                Message msg = Message.obtain();
                                msg.obj = imageFile.getAbsolutePath();
                                msg.what = -1;
                                handler.sendMessage(msg);
                            }

                            @Override
                            public void onSaveFailed(String retFlag, String retMsg) {
                                if(param.isRecycleBitmap){
                                    releaseBitmap();
                                }
                                Message msg = Message.obtain();
                                msg.what = 0;
                                msg.obj = retMsg;
                                handler.sendMessage(msg);
                            }
                        });
                    }
                });
                service.shutdown();
            }

            @Override
            public void onError(int i, String msg) {
                Toast.makeText(CONTEXT, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReady() {
                if (param != null) {
                    graffitiView.setPaintSize(param.stroke);
                    graffitiView.setColor(param.color);
                } else {
                    graffitiView.setPaintSize(10.0f);
                    graffitiView.setColor(Color.RED);
                }
            }

            @Override
            public void onSelectedItem(GraffitiSelectableItem selectableItem, boolean selected) {}

            @Override
            public void onSingleClick() {
                view.onSingleClick();
            }
        });
        graffitiView.setIsDrawableOutside(param != null && param.isDrawOutSide);
        container.addView(graffitiView);
        mTouchSlop = ViewConfiguration.get(CONTEXT).getScaledTouchSlop();
        if (param == null || param.scaleEnable) {
            graffitiView.setOnTouchListener(this);
        }

    }

    /**
     * 设置画笔：涂鸦or文本
     *
     * @param pen
     */
    public void setPen(GraffitiView.Pen pen) {
        graffitiView.setPen(pen);
    }

    public void setColor(@ColorInt int color) {
        graffitiView.setColor(color);
    }

    /**
     * 保存编辑后的图片
     * <p>
     * 即使不编辑也可以保存
     */
    public void saveImage() {
        graffitiView.save();
    }

    /**
     * 添加右下角水印文字
     *
     * @param text
     */
    public void addWaterMark(String text, float textSize) {
        if (text == null || text.trim().length() <= 0) {
            return;
        }
        graffitiView.addWaterMark(text, textSize);
    }

    /**
     * 撤销上一步操作
     */
    public void cancelLast() {
        graffitiView.undo();
    }

    /**
     * 清屏
     */
    public void clear() {
        graffitiView.clear();
    }

    /**
     * 是否有过操作
     *
     * @return
     */
    public boolean isModified() {
        return graffitiView.isModified();
    }

    /**
     * 销毁并回收，同时需要将ViewGroup.removeAllViews()
     */
    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        canvasBitmap = null;
        if (graffitiView != null) {
            graffitiView.release();
        }
        graffitiView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        releaseBitmap();
        onDestroy();
    }

    private void releaseBitmap(){
        try{
            savedbitmap.recycle();
        }catch (Exception e){

        }finally {
            savedbitmap = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float mScale;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchMode = 1;
                mTouchLastX = event.getX();
                mTouchLastY = event.getY();
                return isScaling;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchMode = 0;
                if (!isScaling) {
                    return false;
                }
                isScaling = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isScaling && (graffitiView.getScale() <= 1f || graffitiView.getPen() == GraffitiView.Pen.HAND)) {
                    //未正在缩放 且 (未缩放 或 已缩放但正在涂鸦)
                    return false;
                }

                if (mTouchMode < 2) { // 单点滑动
                    if(graffitiView.getScale() > 1f && graffitiView.isSelectedItem()){
                        return false;
                    }
                    if (mIsBusy) { // 从多点触摸变为单点触摸，忽略该次事件，避免从双指缩放变为单指移动时图片瞬间移动
                        mIsBusy = false;
                        mTouchLastX = event.getX();
                        mTouchLastY = event.getY();
                        return true;
                    }
                    if(graffitiView.getScale() > 1f){
                        float tranX = event.getX() - mTouchLastX;
                        float tranY = event.getY() - mTouchLastY;
                        graffitiView.setTrans(graffitiView.getTransX() + tranX, graffitiView.getTransY() + tranY);
                        mTouchLastX = event.getX();
                        mTouchLastY = event.getY();
                    }
                } else { // 多点
                    float mNewDist = spacing(event);// 两点滑动时的距离
                    float scale = mNewDist / mOldDist;
                    if (Math.abs(mNewDist - mOldDist) >= mTouchSlop) {
                        if (graffitiView.isSelectedItem()) {
                            float size = graffitiView.getSelectedItemSize() * (1+((scale-1)/20));
                            if(size < 20){
                                size = 20;
                            }else if(size > 300){
                                size = 300;
                            }
                            graffitiView.setSelectedItemSize(size);
                        } else {
                            mScale = mOldScale * scale;

                            if (mScale > MAX_SCALE) {
                                mScale = MAX_SCALE;
                            }
                            if (mScale < MIN_SCALE) { // 最小倍数
                                mScale = MIN_SCALE;
                            }

                            // 围绕坐标(0,0)缩放图片
                            graffitiView.setScale(mScale);
                            // 缩放后，偏移图片，以产生围绕某个点缩放的效果
                            float transX = graffitiView.toTransX(mTouchCentreX, mToucheCentreXOnGraffiti);
                            float transY = graffitiView.toTransY(mTouchCentreY, mToucheCentreYOnGraffiti);
                            if(mScale <= 1){
                                transY = 0f;
                            }
                            graffitiView.setTrans(transX, transY);
                        }

                    }
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                isScaling = true;
                mTouchMode -= 1;
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                isScaling = true;
                mTouchMode += 1;
                mOldScale = graffitiView.getScale();
                mOldDist = spacing(event);// 两点按下时的距离
                mTouchCentreX = (event.getX(0) + event.getX(1)) / 2;// 不用减trans
                mTouchCentreY = (event.getY(0) + event.getY(1)) / 2;
                mToucheCentreXOnGraffiti = graffitiView.toX(mTouchCentreX);
                mToucheCentreYOnGraffiti = graffitiView.toY(mTouchCentreY);
                mIsBusy = true; // 标志位多点触摸
                return true;
        }
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private static final class SaveBitmapHandler extends Handler {

        WeakReference<EditPicturePresenter> reference;

        private SaveBitmapHandler(EditPicturePresenter presenter) {
            reference = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            reference.get().view.showProgress(false);
            String str = (String) msg.obj;
            if (msg.what == -1) {
                Intent data = new Intent();
                data.putExtra(EditPicturePresenter.class.getSimpleName(), str);
                reference.get().view.onSaveImageSuccess(str);
            }
        }
    }

    /**
     * 打开输入文字界面
     *
     * @param activity
     * @param graffitiText
     */
    public void createGraffitiText(Activity activity, final GraffitiText graffitiText) {
        if (activity == null) {
            return;
        }
        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        final Dialog dialog;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        ViewGroup container = (ViewGroup) View.inflate(PictureSelector.CONTEXT, R.layout.graffiti_create_text, null);
        dialog.setContentView(container);

        final EditText textView = (EditText) container.findViewById(R.id.graffiti_selectable_edit);
        textView.setTextColor(graffitiView.getColor().getColor());
        final View cancelBtn = container.findViewById(R.id.graffiti_text_cancel_btn);
        final TextView enterBtn = (TextView) container.findViewById(R.id.graffiti_text_enter_btn);

        View viewOrange = container.findViewById(R.id.color_orange);
        View viewBlue = container.findViewById(R.id.color_blue);
        View viewGreen = container.findViewById(R.id.color_green);
        View viewRed = container.findViewById(R.id.color_red);
        View viewWhite = container.findViewById(R.id.color_white);
        viewOrange.setBackground(createShape("#FF9D32"));
        viewWhite.setBackground(createShape("#FFFFFF"));
        viewBlue.setBackground(createShape("#60B3FF"));
        viewRed.setBackground(createShape("#FF7C7C"));
        viewGreen.setBackground(createShape("#26D39D"));
        viewOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setTextColor(Color.parseColor("#FF9D32"));
                graffitiView.setColor(Color.parseColor("#FF9D32"));
            }
        });
        viewBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setTextColor(Color.parseColor("#60B3FF"));
                graffitiView.setColor(Color.parseColor("#60B3FF"));
            }
        });
        viewGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setTextColor(Color.parseColor("#26D39D"));
                graffitiView.setColor(Color.parseColor("#26D39D"));
            }
        });
        viewRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setTextColor(Color.parseColor("#FF7C7C"));
                graffitiView.setColor(Color.parseColor("#FF7C7C"));
            }
        });
        viewWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setTextColor(Color.parseColor("#FFFFFF"));
                graffitiView.setColor(Color.parseColor("#FFFFFF"));
            }
        });


        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    enterBtn.setEnabled(false);
                    enterBtn.setTextColor(0xffb3b3b3);
                } else {
                    enterBtn.setEnabled(true);
                    enterBtn.setTextColor(0xffffffff);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textView.setText(graffitiText == null ? "" : graffitiText.getText());

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (graffitiText == null) {
                    graffitiView.addSelectableItem(new GraffitiText(graffitiView.getPen(), text, 60, graffitiView.getColor().copy(),
                            0, graffitiView.getGraffitiRotateDegree(), graffitiView.getOriginalPivotX(), graffitiView.getOriginalPivotY(), graffitiView.getOriginalPivotX(), graffitiView.getOriginalPivotY()));
                } else {
                    graffitiText.setText(text);
                }
                graffitiView.invalidate();
            }
        });
    }

    public GradientDrawable createShape(String color){
        GradientDrawable drawable = new GradientDrawable();//圆角矩形
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 11f, PictureSelector.CONTEXT.getResources().getDisplayMetrics()));//圆角矩形的圆角，这里指5像素(可以借助UiUtils中的dp和px相互转换)
        drawable.setColor(Color.parseColor(color));
        return drawable;
    }

}
