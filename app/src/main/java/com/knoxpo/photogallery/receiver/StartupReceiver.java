package com.knoxpo.photogallery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.knoxpo.photogallery.service.PollService;
import com.knoxpo.photogallery.storage.QueryPreferences;

/**
 * Created by Tejas Sherdiwala on 12/3/2016.
 * &copy; Knoxpo
 */

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = StartupReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Received Broadcast intent : " + intent.getAction());
        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context,isOn);
    }
}
