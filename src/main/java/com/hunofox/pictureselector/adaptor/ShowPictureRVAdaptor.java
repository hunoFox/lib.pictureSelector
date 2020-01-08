package com.hunofox.pictureselector.adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.beans.PictureBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/8 14:55.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public class ShowPictureRVAdaptor extends RecyclerView.Adapter<ShowPictureRVAdaptor.ShowPicHolder> implements View.OnClickListener{

    private List<PictureBean> datas;
    private RequestOptions options;

    public ShowPictureRVAdaptor(List<PictureBean> datas) {
        if(datas == null){
            this.datas = new ArrayList<>();
        }else{
            this.datas = datas;
        }

        options = new RequestOptions()
                .placeholder(R.drawable.default_picture_selector)
                .override(180, 180)
                .centerCrop();
    }

    @Override
    public ShowPicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_picture_show, parent, false);
        view.setOnClickListener(this);
        return new ShowPicHolder(view);
    }

    @Override
    public void onBindViewHolder(ShowPicHolder hd, int position) {
        hd.itemView.setTag(position);
        PictureBean bean = datas.get(position);
        Glide.with(CONTEXT)
                .load(new File(bean.imagePath))
                .apply(options)
                .into(hd.iv);

        hd.stroke.setVisibility(bean.isChecked?View.VISIBLE:View.GONE);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    static final class ShowPicHolder extends RecyclerView.ViewHolder{
        private final ImageView iv;
        private final View stroke;
        public ShowPicHolder(View v) {
            super(v);
            iv = (ImageView) v.findViewById(R.id.iv);
            stroke = v.findViewById(R.id.iv_stroke);
        }
    }

    @Override
    public void onClick(View v) {
        if(listener != null){
            listener.onItemClick(v, (int)v.getTag());
        }
    }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
}
