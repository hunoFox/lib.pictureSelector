package com.hunofox.pictureselector.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.base.PictureSelector;
import com.hunofox.pictureselector.beans.PaintParams;
import com.hunofox.pictureselector.beans.PictureBean;
import com.hunofox.pictureselector.mvp.presenters.EditPicturePresenter;
import com.hunofox.pictureselector.mvp.views.IEditPictureView;
import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.pictureEditWeidgt.GraffitiView;

import java.io.File;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/9/7 10:45.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 *
 * 必传项picture folder
 * ----------------------------------------------------------------------------------------------------
 */
public class EditPictureFragment extends Fragment implements IEditPictureView, View.OnClickListener {

    public static final String EXTENSION = "_hunoFoxPictureEdit_";

    private boolean isShowing = true;//监听是否正在展示操作按钮

    private PictureBean picture;
    private String folderName;
    private EditPicturePresenter editPresenter;

    private FrameLayout flContainer;
    private View rlTop;
    private View rlBottom;
    private View rlBottomColor;
    private View rlColor;
    private View tvCancel;
    private View tvConfirm;
    private View tvPen;
    private View tvText;
    private View layoutColor;
    private View tvBack;
    private View tvUndo;
    private View tvUndo1;

    private View viewOrange;
    private View viewRed;
    private View viewBlue;
    private View viewGreen;
    private View viewWhite;
    private View ivColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        picture = getArguments().getParcelable("picture");
        folderName = getArguments().getString("folderName");
        editPresenter = new EditPicturePresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_picture, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final File imageFile = new File(picture.imagePath);

        flContainer = (FrameLayout) view.findViewById(R.id.fl_container);
        rlTop = view.findViewById(R.id.rl_top);
        rlBottom = view.findViewById(R.id.rl_bottom);
        rlBottomColor = view.findViewById(R.id.rl_bottom_color);
        rlColor = view.findViewById(R.id.rl_color);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        tvPen = view.findViewById(R.id.tv_pen);
        tvText = view.findViewById(R.id.tv_text);
        viewOrange = view.findViewById(R.id.color_orange);
        viewBlue = view.findViewById(R.id.color_blue);
        viewGreen = view.findViewById(R.id.color_green);
        viewRed = view.findViewById(R.id.color_red);
        viewWhite = view.findViewById(R.id.color_white);
        ivColor = view.findViewById(R.id.iv_color);
        layoutColor = view.findViewById(R.id.layout_color);
        tvBack = view.findViewById(R.id.tv_back);
        tvUndo = view.findViewById(R.id.tv_undo);
        tvUndo1 = view.findViewById(R.id.tv_undo1);

        layoutColor.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        tvUndo.setOnClickListener(this);
        tvUndo1.setOnClickListener(this);
        viewOrange.setOnClickListener(this);
        viewBlue.setOnClickListener(this);
        viewGreen.setOnClickListener(this);
        viewRed.setOnClickListener(this);
        viewWhite.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        tvPen.setOnClickListener(this);
        tvText.setOnClickListener(this);

        viewOrange.setBackground(editPresenter.createShape("#FF9D32"));
        viewWhite.setBackground(editPresenter.createShape("#FFFFFF"));
        viewBlue.setBackground(editPresenter.createShape("#60B3FF"));
        viewRed.setBackground(editPresenter.createShape("#FF7C7C"));
        ivColor.setBackground(editPresenter.createShape("#FF7C7C"));
        viewGreen.setBackground(editPresenter.createShape("#26D39D"));

        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                String fileName = picture.imageName;
                if(fileName.contains(".")){
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                }
                if(fileName.contains(EXTENSION)){
                    fileName = fileName.substring(0, fileName.lastIndexOf("_") + 1) + System.currentTimeMillis() + ".jpg";
                }else{
                    fileName += EXTENSION + System.currentTimeMillis() +".jpg";
                }

                String folder;
                if(FileUtils.exists(imageFile)){
                    String[] paths = imageFile.getParent().split("/");
                    folder = paths[paths.length-1];
                }else{
                    folder = PictureSelector.SAVE_PATH + folderName;
                }

                if(!folder.contains("hunoFoxEdit")){
                    folder = "hunoFoxEdit" + folder;
                }
                folder = (folderName == null || folderName.length() <= 0)?folder:folderName;
                PaintParams params = new PaintParams();
                params.color = Color.parseColor("#FF7C7C");
                params.setSaveFloderName(folder);
                params.setSaveFileName(fileName);

                editPresenter.showImage(resource, flContainer, params);
                editPresenter.setPen(GraffitiView.Pen.TEXT);
            }
        };

        if(FileUtils.exists(imageFile)){
            int[] fix = FileUtils.fixPictureSize(picture.imagePath);
            Glide.with(PictureSelector.CONTEXT)
                    .asBitmap()
                    .load(imageFile)
                    .apply(new RequestOptions().override(fix[0], fix[1]))
                    .into(target);
        }else{
            GlideUrl glideUrl = null;
            if(PictureSelector.GLIDE_BUILDER != null){
                glideUrl = new GlideUrl(picture.imagePath, PictureSelector.GLIDE_BUILDER.build());
            }
            Glide.with(PictureSelector.CONTEXT)
                    .asBitmap()
                    .load(glideUrl==null?picture.imagePath:glideUrl)
                    .into(target);
        }

        showActionBar();
    }

    @Override
    public void onDestroyView() {
        flContainer.removeAllViews();
        editPresenter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if(tvCancel == v){
            getActivity().onBackPressed();
        }else if(tvConfirm == v){
            if(editPresenter.isModified()){
                editPresenter.saveImage();
            }else{
                getActivity().onBackPressed();
            }
        }else if(v == tvPen){
            tvPen.setSelected(true);
            tvText.setSelected(false);
            editPresenter.setPen(GraffitiView.Pen.HAND);
            rlBottom.setVisibility(View.GONE);
            rlBottomColor.setVisibility(View.VISIBLE);
            rlColor.setVisibility(View.GONE);
        }else if(v == tvText){
            tvPen.setSelected(false);
            tvText.setSelected(true);
            dismissActionBar();
            editPresenter.setPen(GraffitiView.Pen.TEXT);
            editPresenter.createGraffitiText(getActivity(), null);
        }else if(v == layoutColor){
            layoutColor.setSelected(!layoutColor.isSelected());
            if(layoutColor.isSelected()){
                rlColor.setVisibility(View.VISIBLE);
            }else{
                rlColor.setVisibility(View.GONE);
            }
        }else if(v == tvBack){
            layoutColor.setSelected(false);
            tvPen.setSelected(false);
            tvText.setSelected(false);
            editPresenter.setPen(GraffitiView.Pen.TEXT);
            rlBottom.setVisibility(View.VISIBLE);
            rlBottomColor.setVisibility(View.GONE);
            rlColor.setVisibility(View.GONE);
        }else if(v == tvUndo || v == tvUndo1){
            layoutColor.setSelected(false);
            if(rlColor.getVisibility() == View.VISIBLE){
                rlColor.setVisibility(View.GONE);
            }
            editPresenter.cancelLast();
        }else if(v == viewOrange){
            layoutColor.setSelected(false);
            if(rlColor.getVisibility() == View.VISIBLE){
                rlColor.setVisibility(View.GONE);
            }
            editPresenter.setColor(Color.parseColor("#FF9D32"));
            ivColor.setBackground(editPresenter.createShape("#FF9D32"));
            rlColor.setVisibility(View.GONE);
        }else if(v == viewWhite){
            layoutColor.setSelected(false);
            if(rlColor.getVisibility() == View.VISIBLE){
                rlColor.setVisibility(View.GONE);
            }
            editPresenter.setColor(Color.parseColor("#FFFFFF"));
            ivColor.setBackground(editPresenter.createShape("#FFFFFF"));
            rlColor.setVisibility(View.GONE);
        }else if(v == viewBlue){
            layoutColor.setSelected(false);
            if(rlColor.getVisibility() == View.VISIBLE){
                rlColor.setVisibility(View.GONE);
            }
            editPresenter.setColor(Color.parseColor("#60B3FF"));
            ivColor.setBackground(editPresenter.createShape("#60B3FF"));
            rlColor.setVisibility(View.GONE);
        }else if(v == viewRed){
            layoutColor.setSelected(false);
            if(rlColor.getVisibility() == View.VISIBLE){
                rlColor.setVisibility(View.GONE);
            }
            editPresenter.setColor(Color.parseColor("#FF7C7C"));
            ivColor.setBackground(editPresenter.createShape("#FF7C7C"));
            rlColor.setVisibility(View.GONE);
        }else if(v == viewGreen){
            layoutColor.setSelected(false);
            if(rlColor.getVisibility() == View.VISIBLE){
                rlColor.setVisibility(View.GONE);
            }
            editPresenter.setColor(Color.parseColor("#26D39D"));
            ivColor.setBackground(editPresenter.createShape("#26D39D"));
            rlColor.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveImageSuccess(String path) {
        File file = new File(path);
        picture.imagePath = path;
        picture.imageDate = System.currentTimeMillis();
        picture.imageSize = file.length();
        picture.imageName = file.getName();
        picture.isChecked = true;
        Intent data = new Intent();
        data.putExtra("picture", picture);
        if(getActivity() != null){
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        }
    }

    @Override
    public void onSingleClick() {
        if(isShowing){
            dismissActionBar();
        }else{
            showActionBar();
        }
    }

    private Toast toast;
    @Override
    public void showProgress(boolean flag) {
        if(flag){
            String text = "正在处理图片，请稍候...";
            if(toast == null){
                toast = Toast.makeText(PictureSelector.CONTEXT, text, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
            }
            toast.setText(text);
            toast.show();
        }else{
            if(toast != null){
                toast.cancel();
            }
        }
    }

    private void dismissActionBar(){
        rlColor.setVisibility(View.GONE);
        rlTop.setVisibility(View.GONE);
        rlBottom.setVisibility(View.GONE);
        rlBottomColor.setVisibility(View.GONE);
        isShowing = false;
    }
    private void showActionBar(){
        if(tvPen.isSelected()){
            if(layoutColor.isSelected()){
                rlColor.setVisibility(View.VISIBLE);
            }else{
                rlColor.setVisibility(View.GONE);
            }

            rlBottomColor.setVisibility(View.VISIBLE);
            rlBottom.setVisibility(View.GONE);
        }else{
            rlColor.setVisibility(View.GONE);
            rlBottomColor.setVisibility(View.GONE);
            rlBottom.setVisibility(View.VISIBLE);
        }
        rlTop.setVisibility(View.VISIBLE);
        isShowing = true;
    }
}
