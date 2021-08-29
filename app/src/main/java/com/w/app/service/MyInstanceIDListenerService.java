package com.w.app.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.w.app.R;
import com.w.app.activity.DashboardActivity;
import com.w.app.activity.LoginActivity;
import com.w.app.utils.Common;
import com.w.app.utils.SharePreference;

import org.json.JSONObject;

public class MyInstanceIDListenerService extends FirebaseMessagingService {

    public String TAG = "MyInstanceIDListenerService";

    private static final int REQUEST_CODE = 1;
    private static int NOTIFICATION_ID = 6578;
    Context ctx;
    String Notification_message;

    public MyInstanceIDListenerService() {
        super();
    }

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        activity = (Activity) getApplicationContext();
        ctx = getApplicationContext();

        Common.INSTANCE.getLog(TAG, "onMessageReceived: " + remoteMessage.toString());
        Common.INSTANCE.getLog(TAG, "onMessageReceived: From" + remoteMessage.getFrom());

        final String Notification_title = remoteMessage.getData().get("title");
        Notification_message = remoteMessage.getData().get("message");
        final String Notification_data = remoteMessage.getData().get("data");
        final String Notification_status = remoteMessage.getData().get("status");

        Common.INSTANCE.getLog("onMessageReceived=== Notification_title", "" + Notification_title);
        Common.INSTANCE.getLog("onMessageReceived=== Notification_message", "" + Notification_message);
        Common.INSTANCE.getLog("onMessageReceived=== Notification_data", "" + Notification_data);
        Common.INSTANCE.getLog("onMessageReceived=== Notification_status", "" + Notification_status);

        // showNotifications(Notification_title, Notification_message, "");

        //onIncomingCalling("","","");
        onResponseBody(Notification_title);
    }

    public void onResponseBody(String strResponseBody) {
        showNotifications("Wallpaper", strResponseBody);
    }


    private void showNotifications(String strTitle, String strMessage) {
        Intent intent=null;
        if(SharePreference.Companion.getBooleanPref(this,SharePreference.Companion.isLogin())){
            intent = new Intent(this, DashboardActivity.class);
        }else {
            intent = new Intent(this, LoginActivity.class);
        }
        String channelId = "channel-01";
        String channelName = "Channel Name";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            manager.createNotificationChannel(mChannel);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(getIcon())
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setContentTitle(strTitle)
                    .setContentText(strMessage);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            mBuilder.setContentIntent(resultPendingIntent);

            manager.notify(NOTIFICATION_ID, mBuilder.build());

        } else {

            PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(strTitle)
                    .setContentText(strMessage)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(getIcon())
                    .build();

            manager.notify(NOTIFICATION_ID, notification);
        }

        NOTIFICATION_ID++;
    }
    private int getIcon() {
        int icon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? R.drawable.ic_stat_name : R.drawable.ic_stat_name;
        return icon;
    }
}