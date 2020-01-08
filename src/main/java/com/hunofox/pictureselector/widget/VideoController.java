package com.hunofox.pictureselector.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Formatter;
import java.util.Locale;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/11/22 13:28.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class VideoController extends MediaController {

    private OnShowOrHideListener listener;
    private MediaPlayerControl mPlayer;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public VideoController(Context context) {
        super(context);
    }

    @Override
    public void show(int timeout) {
        super.show(0);
        if (listener != null) {
            listener.show();
        }
    }

    @Override
    public void hide() {
        super.hide();
        if (listener != null) {
            listener.hide();
        }
    }

    public interface OnShowOrHideListener {
        void show();

        void hide();
    }

    public void setOnShowOrHideListener(OnShowOrHideListener listener) {
        this.listener = listener;
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);

        this.mPlayer = player;
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        try {
            Field mFfwdButton = MediaController.class.getDeclaredField("mFfwdButton");
            mFfwdButton.setAccessible(true);
            ImageButton button = (ImageButton) mFfwdButton.get(this);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = mPlayer.getCurrentPosition();
                    pos += 5000;
                    mPlayer.seekTo(pos);
                    setProgress();
                    show(0);
                }
            });

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    private void setProgress() {
        if (mPlayer == null) {
            return;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();

        ProgressBar mProgress = null;
        TextView mEndTime = null;
        TextView mCurrentTime = null;
        try {
            Field mProgressFiled = MediaController.class.getDeclaredField("mProgress");
            mProgressFiled.setAccessible(true);
            mProgress = (ProgressBar) mProgressFiled.get(this);

            Field mEndTimeFiled = MediaController.class.getDeclaredField("mEndTime");
            mEndTimeFiled.setAccessible(true);
            mEndTime = (TextView) mEndTimeFiled.get(this);

            Field mCurrentTimeFiled = MediaController.class.getDeclaredField("mCurrentTime");
            mCurrentTimeFiled.setAccessible(true);
            mCurrentTime = (TextView) mCurrentTimeFiled.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

}
