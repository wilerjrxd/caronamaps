package com.dragoonssoft.apps.caronacap;

import android.annotation.TargetApi;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

/**
 * Created by wiler on 22/07/2017.
 */
@TargetApi(23)
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    //List messages = new ArrayList();
    //private String Title, Message;
    //List<String> listMsgs = new ArrayList<String>();
    //List<String> listPedidos = new ArrayList<String>();
    //List<String> listSaiu = new ArrayList<String>();
    //String tag;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        ////////////super.onMessageReceived(remoteMessage);
        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String notificationTag = remoteMessage.getNotification().getTag();
        String notificationClickAction = remoteMessage.getNotification().getClickAction();
        String pedidosTab = remoteMessage.getData().get("p_tab");
        //String updateDisponivel = remoteMessage.getData().get("update");

        //NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        NotificationCompat.Builder mBuilder;
        NotificationManager mNotifyMgr;

        mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //if(!listMsgs.contains(notificationBody))
        //    listMsgs.add(notificationBody);

        //inboxStyle.setBigContentTitle("caronaCAP");


        //for(int i=0;i<listMsgs.size();i++) {
        //    inboxStyle.addLine(listMsgs.get(i));
        //    Log.d("CARONA", "MSG: " + listMsgs.toString());
        //}

        /*try {
            JSONObject json = new JSONObject(remoteMessage.getData().toString());
            Title = json.get("title").toString();
            Message = json.get("body").toString();
            messages.add(Message);

        } catch (Exception e) {

        }*/

        //if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new NotificationCompat.Builder(this, "notifications")
                    .setSmallIcon(R.mipmap.ic_launcher_alpha)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    //.setStyle(inboxStyle)
                    .setAutoCancel(true)
                    //.setGroup("caronaCAP")
                    //.setGroupSummary(true)
                    //.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                    //.setDeleteIntent(PendingIntent.getBroadcast(this,101,new Intent(this, NotificationDismissedReceiver.class),PendingIntent.FLAG_CANCEL_CURRENT))
                    .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                    .setOnlyAlertOnce(true);
            //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        /*}else{
            mBuilder = new NotificationCompat.Builder(this, "notifications")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    //.setStyle(inboxStyle)
                    .setAutoCancel(true)
                    //.setGroup("caronaCAP")
                    //.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                    //.setDeleteIntent(PendingIntent.getBroadcast(this,101,new Intent(this, NotificationDismissedReceiver.class),PendingIntent.FLAG_CANCEL_CURRENT))
                    .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                    .setOnlyAlertOnce(true);

        }*/
        //NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        /*String[] events = new String[6];
        inboxStyle.setBigContentTitle("Event tracker details:");
// Moves events into the expanded layout
        for (int i=0; i < events.length; i++) {
            inboxStyle.addLine(events[i]);
        }
// Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);*/

        Intent resultIntent = new Intent(notificationClickAction);
        resultIntent.putExtra("tab", pedidosTab);
        //resultIntent.putExtra("update", updateDisponivel);
        resultIntent.putExtra("click_action", notificationClickAction);


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        android.app.Notification noti = mBuilder.build();
        //noti.flags |= android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;

        int mNotificationId = (int)System.currentTimeMillis();
        mNotifyMgr.notify(notificationTag, 0, noti);
    }

    //private int getNotificationIcon() {
    //    boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
    //    return useWhiteIcon ? R.mipmap.ic_launcher_alpha : R.mipmap.ic_launcher;
    //}

    /*public class NotificationDismissedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            messages.clear();
        }
    }*/
}
