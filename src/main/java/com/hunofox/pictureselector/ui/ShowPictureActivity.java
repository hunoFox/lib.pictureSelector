package com.hunofox.pictureselector.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.adaptor.ShowPictureRVAdaptor;
import com.hunofox.pictureselector.adaptor.ShowPictureVPAdaptor;
import com.hunofox.pictureselector.base.PictureSelector;
import com.hunofox.pictureselector.beans.FolderBean;
import com.hunofox.pictureselector.beans.PictureBean;
import com.hunofox.pictureselector.mvp.presenters.PicturePresenter;
import com.hunofox.pictureselector.mvp.views.IPictureView;
import com.hunofox.pictureselector.utils.FileUtils;
import com.hunofox.pictureselector.widget.SwipeableLayout;
import com.hunofox.pictureselector.widget.photoview.HackyViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：
 * 项目作者：胡玉君
 * 创建日期：2017/9/7 11:32.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * <p>
 * intent需要传参：
 * 1. TAG_ALL       所有要展示的图片 ParcelableArrayList，可不传，若不传则自动查询
 * 2. TAG_SELECTED  所有被选中的图片 ParcelableArrayList
 * 3. TAG_CURRENT   当前所在位置 int，不传默认为0
 *
 * 4. TAG_FOLDER     当前进入时要展示的图片文件夹，可不传，不传时为本机中所有图片
 * ----------------------------------------------------------------------------------------------------
 */
public class ShowPictureActivity extends FragmentActivity implements View.OnClickListener, IPictureView {

    public static final String TAG_ALL = "SHOWPIC_ALL";//所有要图片的集合
    private PicturePresenter presenter;

    public static final String TAG_SELECTED = "SHOWPIC_SELECTED";//要选择图片的集合
    public static final String TAG_CURRENT = "SHOWPIC_PRESSED";//当前点击图片的位置
    public static final String TAG_FOLDER = "SHOWPIC_FOLODER";//当前图片所在文件夹

    private HackyViewPager viewpager;
    private CheckBox cb;
    private TextView btConfirm;
    private TextView btEdit;
    private TextView btText;

    private final List<PictureBean> datas = new ArrayList<>();
    private final List<PictureBean> selectedDatas = new ArrayList<>();
    private ShowPictureRVAdaptor recyclerViewAdaptor;
    private ShowPictureVPAdaptor viewPagerAdaptor;

    //根据点击 显示or隐藏
    private boolean isShowing = true;
    private RelativeLayout rlTop;
    private LinearLayout llBottom;
    private RecyclerView recyclerView;
    private View divider;
    private EditText etDesc;

    private TextView tvBack;

    private RelativeLayout rlBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in_pic, R.anim.fade_out_pic);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_show_picture);

        viewpager = (HackyViewPager) findViewById(R.id.viewpager);
        cb = (CheckBox) findViewById(R.id.cb);
        btConfirm = (TextView) findViewById(R.id.bt_confirm);
        btEdit = (TextView) findViewById(R.id.bt_edit);
        btText = findViewById(R.id.bt_text);
        rlTop = (RelativeLayout) findViewById(R.id.rl_top);
        llBottom = findViewById(R.id.ll_bottom);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        divider = findViewById(R.id.view_divider);
        tvBack = (TextView) findViewById(R.id.tv_back);
        rlBackground = (RelativeLayout) findViewById(R.id.rl_background);
        rlBackground.getBackground().setAlpha(255);
        etDesc = findViewById(R.id.et_desc);

        tvBack.setOnClickListener(this);
        btConfirm.setOnClickListener(this);
        btEdit.setOnClickListener(this);
        cb.setOnClickListener(this);
        btText.setOnClickListener(this);
        etDesc.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                datas.get(viewpager.getCurrentItem()).desc = s.toString();
            }

        });

        final Intent data = getIntent();

        List<PictureBean> parcelableSelectedDatas = data.getParcelableArrayListExtra(TAG_SELECTED);
        selectedDatas.addAll(parcelableSelectedDatas);
        if (selectedDatas.size() > 0) {
            btConfirm.setText("确定(" + selectedDatas.size() + ")");
        }

        //配置ViewPager
        List<PictureBean> parcelableDatas = data.getParcelableArrayListExtra(TAG_ALL);
        int currentPosition = data.getIntExtra(TAG_CURRENT, 0);
        if (parcelableDatas != null && parcelableDatas.size() > 0) {
            configVPAndRV(parcelableDatas, currentPosition);
        }else{
            String folder = data.getStringExtra(TAG_FOLDER);
            presenter = new PicturePresenter(this);
            presenter.initPicturesData(getSupportLoaderManager(), folder);
        }
    }

    private void configVPAndRV(List<PictureBean> parcelableDatas, int currentPosition) {
        datas.addAll(parcelableDatas);
        cb.setChecked(selectedDatas.contains(datas.get(currentPosition)));
        viewPagerAdaptor = new ShowPictureVPAdaptor(this, datas);
        viewpager.setAdapter(viewPagerAdaptor);
        viewpager.setCurrentItem(currentPosition);

        PictureBean bean = datas.get(currentPosition);
        if("Y".equals(bean.isVideo)){
            btEdit.setVisibility(View.GONE);
        }else{
            btEdit.setVisibility(View.VISIBLE);
        }
        if(bean.desc != null && bean.desc.trim().length() > 0 && isShowing){
            etDesc.setVisibility(View.VISIBLE);
            etDesc.setText(bean.desc);
            etDesc.setSelection(bean.desc.length());
        }else{
            etDesc.setVisibility(View.GONE);
            etDesc.setText("");
        }
        tvBack.setText((currentPosition + 1) + "/" + datas.size());

        //配置RecyclerView
        if (selectedDatas.contains(datas.get(currentPosition))) {
            selectedDatas.get(selectedDatas.indexOf(datas.get(currentPosition))).isChecked = true;
        }
        recyclerViewAdaptor = new ShowPictureRVAdaptor(selectedDatas);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(recyclerViewAdaptor);
        if (selectedDatas.contains(datas.get(currentPosition))) {
            recyclerView.scrollToPosition(selectedDatas.indexOf(datas.get(currentPosition)));
        }
        recyclerView.setVisibility(selectedDatas.isEmpty() ? View.GONE : View.VISIBLE);
        divider.setVisibility(selectedDatas.isEmpty() ? View.GONE : View.VISIBLE);

        recyclerViewAdaptor.setOnItemClickListener(new ShowPictureRVAdaptor.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (selectedDatas.get(position).isChecked) return;

                for (PictureBean selectedData : selectedDatas) {
                    selectedData.isChecked = false;
                }
                selectedDatas.get(position).isChecked = true;
                recyclerViewAdaptor.notifyDataSetChanged();

                int pos = datas.indexOf(selectedDatas.get(position));
                if (pos != viewpager.getCurrentItem()) {
                    viewpager.setCurrentItem(pos, false);
                }
            }
        });
        viewPagerAdaptor.setOnItemClickListener(new ShowPictureVPAdaptor.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (isShowing) {
                    rlTop.setVisibility(View.GONE);
                    llBottom.setVisibility(View.GONE);
                } else {
                    if (selectedDatas.size() > 0) {
                        divider.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        divider.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    }

                    PictureBean pictureBean = datas.get(viewpager.getCurrentItem());
                    if(pictureBean.desc != null && pictureBean.desc.trim().length() > 0){
                        etDesc.setVisibility(View.VISIBLE);
                        etDesc.setText(pictureBean.desc);
                        etDesc.setSelection(pictureBean.desc.length());
                    }else{
                        etDesc.setVisibility(View.GONE);
                    }
                    rlTop.setVisibility(View.VISIBLE);
                    llBottom.setVisibility(View.VISIBLE);
                }
                isShowing = !isShowing;
            }
        });
        viewPagerAdaptor.setOnLayoutListener(new SwipeableLayout.OnLayoutCloseListener() {
            @Override
            public void onLayout(int dx, int dy, int alpha) {
                if (Math.abs(dy) > Math.abs(dx)) {
                    rlBackground.getBackground().setAlpha(alpha);
                    if (isShowing) {
                        rlTop.setVisibility(View.GONE);
                        llBottom.setVisibility(View.GONE);
                        isShowing = false;
                    }
                }
            }

            @Override
            public void OnLayoutClosed(SwipeableLayout layout, float currentHeight, float maxHeight) {
                finish();
                overridePendingTransition(R.anim.fade_in_pic, R.anim.trans_out_pic);
            }

            @Override
            public void onBackToStart(int currentAlpha) {
                rlBackground.getBackground().setAlpha(255);
            }
        });

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                cb.setChecked(selectedDatas.contains(datas.get(position)));
                tvBack.setText((position + 1) + "/" + datas.size());
                if("Y".equals(datas.get(position).isVideo)){
                    btEdit.setVisibility(View.GONE);
                }else{
                    btEdit.setVisibility(View.VISIBLE);
                }

                boolean isRecyclerDataChanged = false;
                if (!cb.isChecked()) {
                    //若切换页面后当前页面未被选中 且 RecyclerView中也没有被选中的数据，则isRecyclerDataChanged无需改变
                    for (PictureBean selectedData : selectedDatas) {
                        if (selectedData.isChecked) {
                            selectedData.isChecked = false;
                            isRecyclerDataChanged = true;
                        }
                    }
                } else {
                    isRecyclerDataChanged = true;
                    for (PictureBean selectedData : selectedDatas) {
                        selectedData.isChecked = false;
                    }
                    int selectedPos = selectedDatas.indexOf(datas.get(position));
                    recyclerView.scrollToPosition(selectedPos);
                    selectedDatas.get(selectedPos).isChecked = true;
                }
                if (isRecyclerDataChanged) {
                    recyclerViewAdaptor.notifyDataSetChanged();
                }

                PictureBean pictureBean = datas.get(position);
                if(pictureBean.desc != null && pictureBean.desc.trim().length() > 0){
                    etDesc.setText(pictureBean.desc);
                    etDesc.setSelection(pictureBean.desc.length());
                    if(isShowing){
                        etDesc.setVisibility(View.VISIBLE);
                    }else{
                        etDesc.setVisibility(View.GONE);
                    }
                }else{
                    etDesc.setText("");
                    etDesc.setVisibility(View.GONE);
                }
            }

            public void onPageScrollStateChanged(int state) {}
        });
    }

    public void onClick(View view) {
        if (view == tvBack) {
            onBackPressed();
        } else if (view == btConfirm) {
            if (selectedDatas.isEmpty()) {
                selectedDatas.add(datas.get(viewpager.getCurrentItem()));
            }
            Intent data = new Intent();
            data.putParcelableArrayListExtra(TAG_SELECTED, (ArrayList<? extends Parcelable>) selectedDatas);
            setResult(RESULT_OK, data);
            finish();
        } else if (view == btEdit) {
            Intent data = new Intent(this, EditPictureActivity.class);
            data.putExtra("picture", datas.get(viewpager.getCurrentItem()));
            startActivityForResult(data, 1);
        } else if (view == cb) {
            if (cb.isChecked() && selectedDatas.size() >= 9) {
                cb.setChecked(false);
                Toast.makeText(PictureSelector.CONTEXT, "您一次最多选择9张图片", Toast.LENGTH_SHORT).show();
                return;
            }

            PictureBean bean = datas.get(viewpager.getCurrentItem());
            if (cb.isChecked()) {
                if (!selectedDatas.contains(bean)) {
                    bean.isChecked = true;
                    selectedDatas.add(bean);
                }
            } else {
                if (selectedDatas.contains(bean)) {
                    bean.isChecked = false;
                    selectedDatas.remove(bean);
                }
            }

            if (selectedDatas.isEmpty()) {
                btConfirm.setText("确定");
                recyclerView.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            } else {
                btConfirm.setText("确定(" + selectedDatas.size() + ")");
                recyclerView.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
            }

            recyclerViewAdaptor.notifyDataSetChanged();
            if (bean.isChecked) {
                recyclerView.scrollToPosition(selectedDatas.size() - 1);
            }
        }else if(view == btText){
            etDesc.setVisibility(View.VISIBLE);
            etDesc.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null){
                imm.showSoftInput(etDesc, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                PictureBean picture = data.getParcelableExtra("picture");

                //更新图片显示
                picture.isChecked = true;
                viewPagerAdaptor.changeData(datas.get(viewpager.getCurrentItem()).imagePath);
                datas.add(viewpager.getCurrentItem(), picture);
                tvBack.setText(viewpager.getCurrentItem() + 1 + "/" + datas.size());
                viewPagerAdaptor.notifyDataSetChanged();

                //移除编辑前选中的图片
                String oldName = picture.imageName.substring(0, picture.imageName.indexOf(EditPictureFragment.EXTENSION));
                PictureBean removeBean = null;
                int pos = -1;
                for (PictureBean bean : selectedDatas) {
                    pos++;
                    if (bean.imageName.contains(oldName)) {
                        removeBean = bean;
                        bean.isChecked = false;
                        break;
                    }
                }
                if (removeBean != null) {
                    File file = new File(removeBean.imagePath);
                    selectedDatas.remove(removeBean);
                    if (FileUtils.exists(file) && removeBean.imagePath.contains(EditPictureFragment.EXTENSION)) {
                        //删除之前编辑的图片
                        file.delete();
                    }
                }

                //选中编辑后的图片
                selectedDatas.add(pos > -1 ? pos : 0, picture);
                cb.setChecked(true);
                btConfirm.setText("确定(" + selectedDatas.size() + ")");
                recyclerViewAdaptor.notifyDataSetChanged();
                if (isShowing) {
                    divider.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);

                    PictureBean p = datas.get(viewpager.getCurrentItem());
                    if(p.desc != null && p.desc.trim().length() > 0){
                        etDesc.setVisibility(View.VISIBLE);
                        etDesc.setText(p.desc);
                        etDesc.setSelection(p.desc.length());
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in_pic, R.anim.fade_out_pic);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        finish();
    }

    @Override
    public void notifyDataSetChanged(List<PictureBean> pics, List<FolderBean> folders) {
        configVPAndRV(pics, getIntent().getIntExtra(TAG_CURRENT, 0));
    }

    public void showProgress(boolean flag) {}


}
