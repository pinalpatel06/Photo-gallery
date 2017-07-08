package com.knoxpo.photogallery.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.knoxpo.photogallery.fragment.MainFragment;

public class MainActivity extends ToolbarActivity {

    @Override
    public Fragment getContentFragment() {
        return new MainFragment();
    }

    public static Intent newIntent(Context context){
        return new Intent(context,MainActivity.class);
    }
}
