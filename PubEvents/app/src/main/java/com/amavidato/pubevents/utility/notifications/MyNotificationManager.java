package com.amavidato.pubevents.utility.notifications;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.service.voice.VoiceInteractionSession;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.amavidato.pubevents.R;

import static androidx.core.content.ContextCompat.getSystemService;

public class MyNotificationManager {

    public enum ChannelID {SUGGESTIONS, REMINDER};
    private Activity activity;

    public MyNotificationManager(Activity activity){
        this.activity = activity;
    }

    public void createNotification(ChannelID channelID, String textTitle, String textContent, @Nullable Integer priority){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(activity, channelID.toString())
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority( priority == null ? NotificationCompat.PRIORITY_DEFAULT : priority)
                .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(activity.getResources(),R.mipmap.ic_launcher_foreground));
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground);
            notificationBuilder.setColor(Color.argb(255,0,0,0));
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground);
        }
                //.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuilder.build());
    }

    public void createNotificationChannel(ChannelID channelID, String name, String description, @Nullable Integer importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID.toString(), name,
                    importance == null ? NotificationManager.IMPORTANCE_DEFAULT : importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(activity,NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void deleteNotificationChannel(ChannelID channelID){
        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
// The id of the channel.
        assert notificationManager != null;
        notificationManager.deleteNotificationChannel(channelID.toString());
    }
}
