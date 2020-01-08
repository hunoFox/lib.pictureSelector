package com.hunofox.pictureselector.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.adaptor.PictureSelectAdaptor;
import com.hunofox.pictureselector.beans.FolderBean;
import com.hunofox.pictureselector.beans.PictureBean;
import com.hunofox.pictureselector.mvp.presenters.PicturePresenter;
import com.hunofox.pictureselector.mvp.views.IPictureView;
import com.hunofox.pictureselector.widget.FolderSelectedPopupWindow;

import java.util.ArrayList;
import java.util.List;

import static com.hunofox.pictureselector.base.PictureSelector.MODE_MULTI;
import static com.hunofox.pictureselector.base.PictureSelector.MODE_SINGLE;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/28 9:08.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class PictureSelectFragment extends Fragment implements IPictureView, View.OnClickListener {

    private static final int SHOW_PICTURE = 905;

    private List<PictureBean> pictureData = new ArrayList<>();
    private List<FolderBean> folderData = new ArrayList<>();
    private PictureSelectAdaptor adaptor;

    private PicturePresenter presenter;

    private Button btPreview;
    private Button btSelector;
    private TextView tvBack;
    private FolderSelectedPopupWindow folderWindow;

    private List<PictureBean> list;//当前已选中图片
    private String currentFolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null){
            list = bundle.getParcelableArrayList(PictureSelectFragment.class.getSimpleName());
        }

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        presenter = new PicturePresenter(this);
        adaptor = new PictureSelectAdaptor(pictureData, getSelectMode());
        final int mode = getSelectMode();
        adaptor.setOnItemClickListener(new PictureSelectAdaptor.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(mode == MODE_SINGLE){
                    setPictureSelectedData(pictureData.get(position));
                }else{
                    Intent intent = new Intent(getActivity(), ShowPictureActivity.class);
                    intent.putExtra(ShowPictureActivity.TAG_CURRENT, position);
                    if(pictureData.size() > 0 && pictureData.size() <= 100){
                        intent.putParcelableArrayListExtra(ShowPictureActivity.TAG_ALL, (ArrayList<? extends Parcelable>) pictureData);
                    }
                    intent.putExtra(ShowPictureActivity.TAG_FOLDER, currentFolder);
                    intent.putParcelableArrayListExtra(ShowPictureActivity.TAG_SELECTED, (ArrayList<? extends Parcelable>) adaptor.selectedList);
                    startActivityForResult(intent, SHOW_PICTURE);
                }
            }
        });
        adaptor.setOnPictureSelectChangeListener(new PictureSelectAdaptor.OnPictureSelectChangeListener() {
            @Override
            public void onPictureSelectChange(List<PictureBean> selectedList, PictureBean changedPic, boolean isSelected) {
                if(selectedList == null || selectedList.isEmpty()){
                    btPreview.setEnabled(false);
                    btPreview.setText("确定");
                }else{
                    btPreview.setEnabled(true);
                    btPreview.setText("确定" + "(" + selectedList.size() + ")");
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture_selector, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.initPicturesData(getActivity().getSupportLoaderManager(), null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adaptor);

        btPreview = (Button) view.findViewById(R.id.bt_preview);
        btPreview.setOnClickListener(this);
        btSelector = (Button) view.findViewById(R.id.bt_selector);
        btSelector.setOnClickListener(this);
        tvBack = (TextView) view.findViewById(R.id.tv_back);
        tvBack.setOnClickListener(this);
    }

    //多选 or 单选
    private int getSelectMode(){
        int mode = getArguments() == null?MODE_MULTI:getArguments().getInt(PictureSelectFragment.class.getSimpleName()+"mode", MODE_MULTI);
        return (mode == MODE_MULTI || mode == MODE_SINGLE)?mode:MODE_MULTI;
    }

    @Override
    public void notifyDataSetChanged(List<PictureBean> pics, List<FolderBean> folders) {
        if(list != null && list.size() > 0){
            for(PictureBean bean : list){
                boolean isAdd = true;
                for(PictureBean pic:adaptor.selectedList){
                    if(bean != null && bean.equals(pic)){
                        isAdd = false;
                        break;
                    }
                }
                if(isAdd && bean.imagePath != null && bean.imagePath.length()>0){
                    adaptor.selectedList.add(bean);
                }
            }
            btPreview.setText("确定" + "(" + adaptor.selectedList.size() + ")");
            list = null;
        }

        pictureData.clear();
        pictureData.addAll(pics);
        if(folderWindow == null){
            folderData.clear();
            folderData.addAll(folders);
            folderWindow = new FolderSelectedPopupWindow(getActivity(), folderData);
            folderWindow.setOnSelectListener(new FolderSelectedPopupWindow.OnSelectListener() {
                @Override
                public void onSelectListener(FolderBean folder) {
                    presenter.initPicturesData(getLoaderManager(), folder.folderPath);
                    currentFolder = folder.folderPath;
                    btSelector.setText(folder.folderName);
                    folderWindow.dismiss();
                }
            });
        }
        adaptor.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if(v == btPreview){
            setPictureSelectedData(null);
        }else if(v == btSelector){
            if(folderWindow != null){
                if(folderWindow.isShowing()){
                    folderWindow.dismiss();
                }else{
                    folderWindow.show(btSelector);
                }
            }
        }else if(v == tvBack){
            getActivity().onBackPressed();
        }
    }

    //选中图片的返回给上一个Activity
    private void setPictureSelectedData(PictureBean bean){
        Bundle bundle = new Bundle();
        if(bean != null){
            adaptor.selectedList.add(bean);
        }
        bundle.putParcelableArrayList(PictureSelectFragment.class.getSimpleName(), (ArrayList<? extends Parcelable>) adaptor.selectedList);
        Intent data = new Intent();
        data.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == SHOW_PICTURE && data != null){
                // 看大图之后回来
                List<PictureBean> selectedDatas = data.getParcelableArrayListExtra(ShowPictureActivity.TAG_SELECTED);
                adaptor.selectedList.clear();
                adaptor.selectedList.addAll(selectedDatas);
                adaptor.notifyDataSetChanged();

                if(adaptor.selectedList.size() > 0){
                    btPreview.setText("确定(" + adaptor.selectedList.size() + ")");
                }else{
                    btPreview.setText("确定");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        pictureData.clear();
        folderData.clear();
        if(adaptor != null){
            adaptor.release();
        }
        if(folderWindow != null){
            folderWindow.release();
        }
        super.onDestroy();
        System.gc();
    }

    @Override
    public void showProgress(boolean flag) {}
}
