package com.hunofox.pictureselector.adaptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.base.PictureSelector;
import com.hunofox.pictureselector.beans.PictureBean;
import com.hunofox.pictureselector.listeners.VideoGestureDetector;
import com.hunofox.pictureselector.ui.ShowVideoActivity;
import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.SwipeableLayout;
import com.hunofox.pictureselector.widget.photoview.OnOutsidePhotoTapListener;
import com.hunofox.pictureselector.widget.photoview.OnPhotoTapListener;
import com.hunofox.pictureselector.widget.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/8 11:55.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class ShowPictureVPAdaptor extends PagerAdapter {

    private final List<PictureBean> datas;
    private final RequestOptions options;
    private final GestureDetector detector;

    public ShowPictureVPAdaptor(Context context, List<PictureBean> datas){
        if(datas != null && !datas.isEmpty())
            this.datas = datas;
        else
            this.datas = new ArrayList<>();

        Point point = new Point();
        WindowManager manager = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getSize(point);

        detector = new GestureDetector(context, new VideoGestureDetector());
        options = new RequestOptions().placeholder(R.drawable.default_picture_selector).error(R.drawable.default_picture_selector);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
//        View view = "Y".equals(datas.get(position).isVideo)?createVideoView(container, position):createPhotoView(container, position);
        View view = createPhotoView(container, position);
//        container.addView(view);
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    //设置Adaptor的Item点击事件
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    //设置上滑事件
    private SwipeableLayout.OnLayoutCloseListener onLayoutListener;
    public void setOnLayoutListener(SwipeableLayout.OnLayoutCloseListener listener){
        this.onLayoutListener = listener;
    }

    private String currentPath;
    public void changeData(String path){
        currentPath = path;
    }

    @Override
    public int getItemPosition(Object object) {
        View view = (View) object;
        if(view.getTag() == currentPath){
            currentPath = null;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    private View createPhotoView(final ViewGroup container, final int position){
        final View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_vp_picture_show, container, false);
        final PictureBean bean = datas.get(position);
        final PhotoView photoView = (PhotoView) view.findViewById(R.id.photoView);
        final String imagePath = bean.imagePath;
        view.setTag(imagePath);
        int[] fix = FileUtils.fixPictureSize(imagePath);
        Glide.with(CONTEXT)
                .load(new File(imagePath))
                .apply(options.override(fix[0], fix[1]))
                .into(photoView);

        //设置Adaptor的点击事件
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });
        photoView.setOnOutsidePhotoTapListener(new OnOutsidePhotoTapListener() {
            @Override
            public void onOutsidePhotoTap(ImageView imageView) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });
//        photoView.setOnSingleFlingListener(new OnSingleFlingListener() {
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                Logger.d("onFling", velocityY+";" + flingY + "(ShowPictureVPAdaptor.java:93)");
//                container.setEnabled(true);
//                if(Math.abs(flingY) > windowY/8){
//                    ((Activity) container.getContext()).finish();
//                    return false;
//                }
//                flingY = 0;
//                container.layout(0, 0, windowX, windowY);
//                return false;
//            }
//        });
//        photoView.setOnViewDragListener(new OnViewDragListener() {
//            @Override
//            public void onDrag(float dx, float dy) {
//                //只能在未放大且上滑时调用
//                if(photoView.getScale() > 1 || Math.abs(dx) > Math.abs(dy) || dy >= 0) return;
//                flingY = (int)(flingY + 1.5*dy);
//                container.layout(0, flingY, windowX, windowY+flingY);
//                container.setEnabled(false);
//            }
//        });

        final SwipeableLayout layout = (SwipeableLayout) view.findViewById(R.id.layout_swipeable);
        layout.setOnLayoutCloseListener(new SwipeableLayout.OnLayoutCloseListener() {
            @Override
            public void onLayout(int dx, int dy, int alpha) {
                if(onLayoutListener != null){
                    onLayoutListener.onLayout(dx, dy, alpha);
                }
//                if(container.getContext() instanceof ShowPictureActivity){
//                    int alpha = (int) (1f - Math.abs((float)dy/(float) layout.getHeight()) * 255);
//                    ((ShowPictureActivity) container.getContext()).rlBackground.getBackground().setAlpha(alpha);
//                }
            }

            @Override
            public void OnLayoutClosed(SwipeableLayout layout, float current, float maxHeight) {
                if(onLayoutListener != null){
                    onLayoutListener.OnLayoutClosed(layout, current, maxHeight);
                }
//                if(container.getContext() instanceof Activity)
//                    ((Activity) container.getContext()).finish();
            }

            @Override
            public void onBackToStart(int currentAlpha) {
                if(onLayoutListener != null){
                    onLayoutListener.onBackToStart(currentAlpha);
                }
            }
        });

        final View layoutVideo = view.findViewById(R.id.layout_video);
        final View ivVideo = view.findViewById(R.id.iv_video);
        if("Y".equals(bean.isVideo)){
            layoutVideo.setVisibility(View.VISIBLE);
            layoutVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null){
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
            ivVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //播放
                    Intent intent = new Intent(container.getContext(), ShowVideoActivity.class);
                    intent.putExtra(ShowVideoActivity.class.getSimpleName(), bean.imagePath);
                    intent.putExtra("title", bean.imageName);
                    container.getContext().startActivity(intent);
                }
            });
        }else{
            layoutVideo.setVisibility(View.GONE);
            layoutVideo.setOnClickListener(null);
            ivVideo.setOnClickListener(null);
        }


        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createVideoView(final ViewGroup container, final int position){
        final View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_vp_video_show, container, false);
        final VideoView vv = view.findViewById(R.id.videoView);

        vv.setVideoPath(datas.get(position).imagePath);
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                vv.start();
            }
        });
        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(PictureSelector.CONTEXT, "视频播放出错", Toast.LENGTH_LONG).show();
                vv.stopPlayback();
                vv.resume();
                return true;
            }
        });
        detector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(position);
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(vv.isPlaying()){
                    vv.pause();
                }else{
                    vv.start();
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });
        vv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
        vv.start();

        return view;
    }
}
