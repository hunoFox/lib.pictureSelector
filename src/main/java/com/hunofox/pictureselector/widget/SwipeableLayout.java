package com.hunofox.pictureselector.widget;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

/**
 * Created by Serhat Surguvec on 17/09/15.
 */
@SuppressWarnings("DefaultFileTemplate")
public class SwipeableLayout extends FrameLayout {

    private int currentAlpha = 0;

    public interface OnLayoutCloseListener {

        void onLayout(int dx, int dy, int alpha);

        void OnLayoutClosed(SwipeableLayout layout, float currentHeight, float maxHeight);

        void onBackToStart(int currentAlpha);
    }

    enum Direction {
        UP_DOWN,
        LEFT_RIGHT,
        NONE
    }

    private Direction direction = Direction.NONE;
    private int previousFingerPositionY;
    private int previousFingerPositionX;
    private int baseLayoutPosition;
    private boolean isScrollingUp;
    private boolean isLocked = false;
    private OnLayoutCloseListener listener;


    public SwipeableLayout(Context context) {
        super(context);
    }

    public SwipeableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwipeableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (isLocked) {
            return false;
        } else {
            final int y = (int) ev.getRawY();
            final int x = (int) ev.getRawX();


            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {

                previousFingerPositionX = x;
                previousFingerPositionY = y;

            } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {


                int diffY = y - previousFingerPositionY;
                int diffX = x - previousFingerPositionX;

                if (Math.abs(diffX) + 50 < Math.abs(diffY)) {
                    return true;
                }

            }

            return false;
        }

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {

        if (!isLocked) {

            final int y = (int) ev.getRawY();
            final int x = (int) ev.getRawX();

            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {

                currentAlpha = 0;

                previousFingerPositionX = x;
                previousFingerPositionY = y;
                baseLayoutPosition = (int) this.getY();

            } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {


                int diffY = y - previousFingerPositionY;
                int diffX = x - previousFingerPositionX;

                if(diffY>0) return true;

                if (direction == Direction.NONE) {
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        direction = Direction.LEFT_RIGHT;
                    } else if (Math.abs(diffX) < Math.abs(diffY)) {
                        direction = Direction.UP_DOWN;
                    } else {
                        direction = Direction.NONE;
                    }
                }

                if (direction == Direction.UP_DOWN) {
                    isScrollingUp = diffY <= 0;
                    this.setY(baseLayoutPosition + diffY);
                    requestLayout();
                    int alpha = (int) ((1f - (Math.abs((float)diffY/(float)this.getHeight()))) * 255);
                    currentAlpha = alpha;
                    if(listener != null){
                        listener.onLayout(0, diffY, alpha);
                    }
                    return true;
                }

            } else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {

                if (direction == Direction.UP_DOWN) {

                    if (isScrollingUp) {

                        int height = this.getHeight();

                        if (Math.abs(this.getY()) > (height / 6)) {

                            if (listener != null) {
                                listener.OnLayoutClosed(this, this.getY(), height);
                            }
                            return true;
                        }

                    } else {

                        int height = this.getHeight();

                        if (Math.abs(this.getY()) > (height / 6)) {
                            if (listener != null) {
                                listener.OnLayoutClosed(this, this.getY(), height);
                            }
                            return true;
                        }
                    }

                    if(listener != null){
                        listener.onBackToStart(currentAlpha);
                    }

                    ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(this, "y", this.getY(), 0);
                    positionAnimator.setDuration(300);
                    positionAnimator.start();

                    direction = Direction.NONE;
                    currentAlpha = 0;
                    return true;
                }

                currentAlpha = 0;
                direction = Direction.NONE;
            }

            return true;

        }

        return false;
    }

    public void setOnLayoutCloseListener(OnLayoutCloseListener closeListener) {
        this.listener = closeListener;
    }

    public void lock() {
        isLocked = true;
    }

    public void unLock() {
        isLocked = false;
    }

}
