package com.example.chatapp.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.chatapp.Activities.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Objects;

public class FirebaseMessaging extends FirebaseMessagingService {
    String UID ;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() ;
        String sent = remoteMessage.getData().get("sent");
        assert sent != null;
        if(sent.equals(UID)){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                sendOAndAboveNotification(remoteMessage) ;
            else
                sendNormalNotification(remoteMessage);
        }

    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this , ChatActivity.class);
        Bundle bundle = new Bundle() ;
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this  ,i ,intent, PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(Integer.parseInt(icon)).setContentText(body)
                .setContentTitle(title).setAutoCancel(true).setSound(soundUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0 ;
        if (i>0){
            j=i ;
        }
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManager.notify(m,builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this , ChatActivity.class);
        Bundle bundle = new Bundle() ;
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this  ,i ,intent, PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ;
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this) ;
        Notification.Builder builder  = notification1.getONotifications(title , body ,pendingIntent , soundUri , icon) ;

        int j = 0 ;
        if (i>0){
            j=i ;
        }
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notification1.getManager().notify(m,builder.build());
    }
}
