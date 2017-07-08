package com.knoxpo.photogallery.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.knoxpo.photogallery.R;
import com.knoxpo.photogallery.activity.MainActivity;
import com.knoxpo.photogallery.model.GalleryItem;
import com.knoxpo.photogallery.network.FlickrFetch;
import com.knoxpo.photogallery.storage.QueryPreferences;

import java.util.List;

/**
 * Created by Tejas Sherdiwala on 12/2/2016.
 * &copy; Knoxpo
 */

public class PollService extends IntentService {
    private static final String TAG = PollService.class.getSimpleName();
    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION",
                                PERM_PRIVATE = "com.knoxpo.android.photogallery.PRIVATE",
                                REQUEST_CODE = "REQUEST_CODE",
                                NOTIFICATION = "NOTIFICATION";
    //private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    private static final long POLL_INTERVAL = 1000*60;



    public static Intent newIntent(Context context){
        return new Intent(context,PollService.class);
    }

    public PollService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!isNetworkAvailableOrConnected()){
            return;
        }else{
            String query = QueryPreferences.getStoredQuery(this);
            String lastResultId = QueryPreferences.getPrefLastResultId(this);
            List<GalleryItem> items;
            if(query == null){
                items = new FlickrFetch().fetchRecentPhotos();
            }else{
                items = new FlickrFetch().searchPhotos(query);
            }

            if(items.size()==0){
                return;
            }
            String resultId = items.get(0).getId();

            if(resultId.equals(lastResultId)){
                Log.i(TAG,"Got last result"+resultId);
            }else{
                Log.i(TAG,"Got a new result" + resultId);
                Resources resources = getResources();
                Intent i = MainActivity.newIntent(this);

                PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

                Notification notification = new NotificationCompat.Builder(this)
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_picture_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();
                showBackgroundNotification(0,notification);
            }
            QueryPreferences.setPrefLastResultId(this,resultId);
        }
    }

    private void showBackgroundNotification(int requestCode,Notification notification){
        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE,requestCode);
        intent.putExtra(NOTIFICATION,notification);
        sendOrderedBroadcast(intent,PERM_PRIVATE,null,null,
                Activity.RESULT_OK, null,null);
    }

    private boolean isNetworkAvailableOrConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() !=null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    public static void setServiceAlarm(Context context,boolean isOn){
        Intent intent = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,intent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),POLL_INTERVAL,pi);
        }else{
            alarmManager.cancel(pi);
            pi.cancel();
        }
        QueryPreferences.setAlarmOn(context,isOn);

    }

    public static boolean isServiceAlarmOn(Context context){
        Intent intent = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,intent,PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }
}
