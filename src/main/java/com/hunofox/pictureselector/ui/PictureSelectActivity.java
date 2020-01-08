package com.hunofox.pictureselector.ui;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.hunofox.pictureselector.R;

public class PictureSelectActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_select);

        if(savedInstanceState == null){
            PictureSelectFragment fragment = new PictureSelectFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment, PictureSelectFragment.class.getSimpleName()).commit();

        }
    }
}
