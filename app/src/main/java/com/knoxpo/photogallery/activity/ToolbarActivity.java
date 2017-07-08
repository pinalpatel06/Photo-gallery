package com.knoxpo.photogallery.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.knoxpo.photogallery.R;

/**
 * Created by Tejas Sherdiwala on 12/1/2016.
 * &copy; Knoxpo
 */

public abstract class ToolbarActivity extends SingleFragmentActivity {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setSupportActionBar(mToolbar);
    }
    private void init(){
        mToolbar = (Toolbar) findViewById(getToolbarId());
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_toolbar;
    }

    protected int getToolbarId(){
        return R.id.toolbar;
    }
}
