package com.hunofox.pictureselector.adaptor;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.base.PictureSelector;
import com.hunofox.pictureselector.beans.PictureBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;
import static com.hunofox.pictureselector.base.PictureSelector.MODE_MULTI;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/28 10:10.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class PictureSelectAdaptor extends RecyclerView.Adapter<PictureSelectAdaptor.PictureHolder> implements View.OnClickListener{

    private final int pictureWidth;
    private final RelativeLayout.LayoutParams params;

    private final int mode;
    private int maxCount;
    private final List<PictureBean> datas;
    public final List<PictureBean> selectedList = new ArrayList<>();
    private final RequestOptions options;

    public PictureSelectAdaptor(List<PictureBean> datas, int selectMode) {
        if(datas == null){
            this.datas = new ArrayList<>();
        }else{
            this.datas = datas;
        }
        mode = selectMode;
        if(mode == MODE_MULTI){
            maxCount = 9;//默认最多一次选择9张图片
        }else{
            maxCount = 1;
        }

        WindowManager wm = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        pictureWidth = size.x/4;
        options = new RequestOptions().override(pictureWidth, pictureWidth).centerCrop();

        params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                pictureWidth
        );
    }

    @Override
    public PictureHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picture_selector, parent, false);
        view.setOnClickListener(this);
        return new PictureHolder(view, params);
    }

    @Override
    public void onBindViewHolder(final PictureHolder hd, int position) {
        hd.itemView.setTag(position);
        final PictureBean bean = datas.get(position);
        Glide.with(CONTEXT)
                .load(new File(bean.imagePath))
                .apply(options)
                .into(hd.iv);

        //调整整体布局的宽高，使他与ImageView协调
//        hd.fl.setLayoutParams(params);
//        hd.iv.setLayoutParams(params);

        //设置选择框 以后要改为ImageView
        hd.cb.setVisibility(mode==MODE_MULTI?View.VISIBLE:View.GONE);
        hd.cb.setChecked(selectedList.contains(bean));
        hd.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedList.size() >= maxCount && hd.cb.isChecked()){
                    hd.cb.setChecked(false);
                    Toast.makeText(PictureSelector.CONTEXT, "您一次最多选择" + maxCount + "张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(hd.cb.isChecked()){
                    selectedList.add(bean);
                }else{
                    selectedList.remove(bean);
                }

                if(listener != null){
                    listener.onPictureSelectChange(selectedList, bean, hd.cb.isChecked());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    /**
     * 设置一次可以选择几张图片
     */
    public void setMaxSelectCount(int selectCount){
        this.maxCount = selectCount;
        if(maxCount < 1){
            if(mode == MODE_MULTI){
                maxCount = 9;
            }else{
                maxCount = 1;
            }
        }
    }

    public void release() {
        selectedList.clear();
    }

    static class PictureHolder extends RecyclerView.ViewHolder{
        private final CheckBox cb;
        private final ImageView iv;
        private final RelativeLayout fl;
        private PictureHolder(View itemView, ViewGroup.LayoutParams params) {
            super(itemView);
            cb = (CheckBox) itemView.findViewById(R.id.cb);
            iv = (ImageView) itemView.findViewById(R.id.iv);
            fl = (RelativeLayout) itemView.findViewById(R.id.fl);

            fl.setLayoutParams(params);
        }
    }

    private OnPictureSelectChangeListener listener;
    public void setOnPictureSelectChangeListener(OnPictureSelectChangeListener listener){
        this.listener = listener;
    }
    public interface OnPictureSelectChangeListener{
        /**
         * 选中状态发生改变时
         *
         * @param selectedList 被选中的图片集合
         * @param changedPic 发生改变的数据
         * @param isSelected 是否被选中了
         */
        void onPictureSelectChange(List<PictureBean> selectedList, PictureBean changedPic, boolean isSelected);
    }

    private OnItemClickListener clickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.clickListener = listener;
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    @Override
    public void onClick(View v) {
        if(clickListener != null){
            clickListener.onItemClick(v, (int)v.getTag());
        }
    }
}
