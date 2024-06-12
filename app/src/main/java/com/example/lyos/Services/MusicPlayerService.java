package com.example.lyos.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.lyos.MainActivity;
import com.example.lyos.R;

public class MusicPlayerService extends Service {
    private static final String CHANNEL_ID = "MusicPlayerChannel";
    private static final String ACTION_PAUSE_OR_PLAY = "PAUSE_OR_PLAY_ACTION";
    private static final String ACTION_PREVIOUS = "PREVIOUS_ACTION";
    private static final String ACTION_NEXT = "NEXT_ACTION";
    private static final String INTERNAL_ACTION_PAUSE_OR_PLAY = "INTERNAL_PAUSE_OR_PLAY_ACTION";
    private static final String INTERNAL_ACTION_PREVIOUS = "INTERNAL_PREVIOUS_ACTION";
    private static final String INTERNAL_ACTION_NEXT = "INTERNAL_NEXT_ACTION";
    private static final String TAG = "MusicPlayerService";
    private RemoteViews remoteViews;
    private boolean isReceiverRegistered = false;

    // Phương thức để cung cấp tham chiếu đến dịch vụ từ MainActivity
    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);
            if (ACTION_PAUSE_OR_PLAY.equals(action)) {
                Intent broadcastIntent = new Intent(INTERNAL_ACTION_PAUSE_OR_PLAY).setPackage(getPackageName());
                context.sendBroadcast(broadcastIntent);
            } else if (ACTION_PREVIOUS.equals(action)) {
                Intent broadcastIntent = new Intent(INTERNAL_ACTION_PREVIOUS).setPackage(getPackageName());
                context.sendBroadcast(broadcastIntent);
            } else if (ACTION_NEXT.equals(action)) {
                Intent broadcastIntent = new Intent(INTERNAL_ACTION_NEXT).setPackage(getPackageName());
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
            isReceiverRegistered = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        createNotification();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        if (isReceiverRegistered) {
            unregisterReceiver(broadcastReceiver);
            isReceiverRegistered = false;
        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        return new LocalBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Service unbound");
        return super.onUnbind(intent);
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

    private void createNotification() {
        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_player_layout);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        remoteViews.setOnClickPendingIntent(R.id.stopButtonPlayback, getPendingIntent(this, ACTION_PAUSE_OR_PLAY));
        remoteViews.setOnClickPendingIntent(R.id.preButtonPlayback, getPendingIntent(this, ACTION_PREVIOUS));
        remoteViews.setOnClickPendingIntent(R.id.nextButtonPlayback, getPendingIntent(this, ACTION_NEXT));

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.lyos)
                .setCustomContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);
    }

    private PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(action).setPackage(getPackageName());
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    // Phương thức để cập nhật giao diện của thông báo từ MainActivity
    public void updateNotificationUI(Bitmap img, String title, String userName, String duration, boolean isPlaying) {
        // Kiểm tra nếu remoteViews
        if (remoteViews == null) {
            return;
        }

        remoteViews.setImageViewBitmap(R.id.imageViewNowPlaying, img);
        remoteViews.setTextViewText(R.id.textViewTitle, title);
        remoteViews.setTextViewText(R.id.textViewUserName, userName);
        remoteViews.setTextViewText(R.id.textViewDuration, duration);

        if (isPlaying) {
            remoteViews.setImageViewResource(R.id.stopButtonPlayback, R.drawable.pause_button);
        } else {
            remoteViews.setImageViewResource(R.id.stopButtonPlayback, R.drawable.play_button);
        }
        // Cập nhật thông báo
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.lyos)
                    .setCustomContentView(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .build());
        }
    }

    // Phương thức để cập nhật tiến trình của bài hát và thời lượng đã phát
    public void updateProcessAndDuration(int currentPosition, int totalDuration) {
        // Kiểm tra nếu remoteViews null
        if (remoteViews == null) {
            return;
        }

        remoteViews.setProgressBar(R.id.progressBarDuration, totalDuration, currentPosition, false);

        // Tính thời gian còn lại
        int remainingTime = totalDuration - currentPosition;

        // Chuyển đổi thời gian còn lại thành định dạng phút:giây
        int minutes = remainingTime / 60000;
        int seconds = (remainingTime % 60000) / 1000;

        // Hiển thị thời lượng đã phát trên textViewDuration
        remoteViews.setTextViewText(R.id.textViewDuration, String.format("%02d:%02d", minutes, seconds));

        // Cập nhật thông báo
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.lyos)
                    .setCustomContentView(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .build());
        }
    }

    public void updatePlayPauseButton(boolean isPlaying) {
        // Kiểm tra nếu remoteViews null
        if (remoteViews == null) {
            return;
        }
        if (isPlaying) {
            remoteViews.setImageViewResource(R.id.stopButtonPlayback, R.drawable.pause_button);
        } else {
            remoteViews.setImageViewResource(R.id.stopButtonPlayback, R.drawable.play_button);
        }

        // Cập nhật thông báo
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.lyos)
                    .setCustomContentView(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .build());
        }
    }
}
