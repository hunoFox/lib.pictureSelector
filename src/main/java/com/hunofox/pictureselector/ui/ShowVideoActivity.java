package com.hunofox.pictureselector.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.base.PictureSelector;
import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.VideoController;

import java.io.File;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/11/22 11:02.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class ShowVideoActivity extends FragmentActivity implements MediaPlayer.OnPreparedListener, View.OnClickListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, VideoController.OnShowOrHideListener {

    private String videoPath;
    private String title;

    private LinearLayout layoutStatus;
    private TextView tvTitle;
    private VideoView vv;

    private VideoController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in_pic, R.anim.fade_out_pic);
        setContentView(R.layout.activity_video_show);

        tvTitle = findViewById(R.id.tv_title);
        layoutStatus = findViewById(R.id.layout_status);
        vv = findViewById(R.id.videoView);
        controller = new VideoController(this);
        vv.setMediaController(controller);
        controller.setOnShowOrHideListener(this);

        videoPath = getIntent().getStringExtra(ShowVideoActivity.class.getSimpleName());
        title = getIntent().getStringExtra("title");
        if(title == null || TextUtils.isEmpty(title.trim())){
            File file = new File(videoPath);
            if(FileUtils.exists(file)){
                title = file.getName();
            }else{
                title = "";
            }
        }
        tvTitle.setText(title);
        vv.setVideoPath(videoPath);

        vv.setOnPreparedListener(this);
        vv.setOnCompletionListener(this);
        vv.setOnErrorListener(this);
        tvTitle.setOnClickListener(this);

        vv.start();
    }

    @Override
    public void onClick(View v) {
        if(v == tvTitle){
            onBackPressed();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {}

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(PictureSelector.CONTEXT, "视频播放出错", Toast.LENGTH_LONG).show();
//        vv.stopPlayback();
//        vv.resume();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        vv.resume();
        vv.pause();
        controller.show(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!vv.isPlaying()){
            vv.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        vv.pause();
    }

    @Override
    protected void onDestroy() {
        vv.stopPlayback();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in_pic, R.anim.fade_out_pic);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in_pic, R.anim.fade_out_pic);
    }

    @Override
    public void show() {
        layoutStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        layoutStatus.setVisibility(View.GONE);
    }

}
