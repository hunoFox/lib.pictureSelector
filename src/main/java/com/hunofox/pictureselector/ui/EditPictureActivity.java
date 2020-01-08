package com.hunofox.pictureselector.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.hunofox.pictureselector.R;
import com.hunofox.pictureselector.beans.PictureBean;

/**
 * 项目名称：kcwxApp
 * 项目作者：胡玉君
 * 创建日期：2018/9/5 8:42.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class EditPictureActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.fragment_edit_picture);

        PictureBean bean = getIntent().getParcelableExtra("picture");
        String folderName = getIntent().getStringExtra("folderName");

        if(savedInstanceState == null){
            EditPictureFragment fragment = new EditPictureFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("picture", bean);
            bundle.putString("folderName", folderName);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_container, fragment, EditPictureFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
