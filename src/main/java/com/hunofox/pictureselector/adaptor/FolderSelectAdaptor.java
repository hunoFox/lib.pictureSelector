package com.hunofox.pictureselector.adaptor;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.beans.FolderBean;

import java.io.File;
import java.util.List;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;

/**
 * 项目名称：MyLibApp
 * 项目作者：胡玉君
 * 创建日期：2017/9/19 13:37.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class FolderSelectAdaptor extends RecyclerView.Adapter<FolderSelectAdaptor.FolderHolder> implements View.OnClickListener{

    private final List<FolderBean> datas;
    private int pictureWidth;
    private int selectedPosition = 0;
    private RequestOptions options;

    public FolderSelectAdaptor(List<FolderBean> datas) {
        this.datas = datas;

        WindowManager wm = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        pictureWidth = size.x/3;
        options = new RequestOptions().placeholder(R.drawable.default_picture_selector)
                .override(pictureWidth, pictureWidth)
                .centerCrop();
    }

    @Override
    public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder_selector, parent, false);
        view.setOnClickListener(this);
        return new FolderHolder(view);
    }

    @Override
    public void onBindViewHolder(FolderHolder hd, int position) {
        hd.itemView.setTag(position);

        FolderBean bean = datas.get(position);

        //加载文件夹封面
        Glide.with(CONTEXT)
                .load(new File(bean.picture.imagePath))
                .apply(options)
                .into(hd.iv);

        //文件夹描述
        hd.tvName.setText(bean.folderName);
        hd.tvCount.setVisibility((bean.pics == null || bean.pics.isEmpty())?View.GONE:View.VISIBLE);
        hd.tvCount.setText((bean.pics == null || bean.pics.isEmpty())?"0张":bean.pics.size() + "张");

        //被选中的图片
        hd.cb.setChecked(selectedPosition == position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void onClick(View v) {
        setSelectedPosition((int) v.getTag());
        if(listener != null){
            listener.onItemClick(v, (int) v.getTag());
        }
    }

    //设置被选中的位置
    public void setSelectedPosition(int selectedPosition){
        if(this.selectedPosition == selectedPosition) return;
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    static final class FolderHolder extends RecyclerView.ViewHolder{

        private final ImageView iv;
        private final TextView tvName;
        private final TextView tvCount;
        private final CheckBox cb;

        public FolderHolder(View v) {
            super(v);

            iv = (ImageView) v.findViewById(R.id.iv);
            tvName = (TextView) v.findViewById(R.id.tv_name);
            tvCount = (TextView) v.findViewById(R.id.tv_count);
            cb = (CheckBox) v.findViewById(R.id.cb);
        }
    }

    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    public interface OnItemClickListener{
        void onItemClick(View itemView, int position);
    }
}
