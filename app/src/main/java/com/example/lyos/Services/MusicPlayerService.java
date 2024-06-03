package com.example.lyos.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.lyos.MainActivity;
import com.example.lyos.R;

public class MusicPlayerService extends Service {
    private static final String CHANNEL_ID = "MusicPlayerChannel";
    private static final String ACTION_PAUSE_OR_PLAY = "PAUSE_OR_PLAY_ACTION";
    private static final String ACTION_PREVIOUS = "PREVIOUS_ACTION";
    private static final String ACTION_NEXT = "NEXT_ACTION";
    private static final String TAG = "MusicPlayerService";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);
            if (ACTION_PAUSE_OR_PLAY.equals(action)) {
                Intent broadcastIntent = new Intent(ACTION_PAUSE_OR_PLAY).setPackage(getPackageName());
                context.sendBroadcast(broadcastIntent);
            } else if (ACTION_PREVIOUS.equals(action)) {
                Intent broadcastIntent = new Intent(ACTION_PREVIOUS).setPackage(getPackageName());
                context.sendBroadcast(broadcastIntent);
            } else if (ACTION_NEXT.equals(action)) {
                Intent broadcastIntent = new Intent(ACTION_NEXT).setPackage(getPackageName());
                context.sendBroadcast(broadcastIntent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        createNotificationChannel();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PAUSE_OR_PLAY);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_NEXT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        @SuppressLint("RemoteViewLayout")
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_player_layout);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        remoteViews.setOnClickPendingIntent(R.id.stopButtonPlayback, getPendingIntent(this, ACTION_PAUSE_OR_PLAY));
        remoteViews.setOnClickPendingIntent(R.id.preButtonPlayback, getPendingIntent(this, ACTION_PREVIOUS));
        remoteViews.setOnClickPendingIntent(R.id.nextButtonPlayback, getPendingIntent(this, ACTION_NEXT));

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.lyos)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // Hiển thị đầy đủ nội dung trên màn hình khóa
                .setOngoing(true)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        unregisterReceiver(broadcastReceiver);
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Channel for Music Player Service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Notification channel created");
            } else {
                Log.e(TAG, "NotificationManager is null, failed to create notification channel");
            }
        }
    }

    private PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(action).setPackage(getPackageName());
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
