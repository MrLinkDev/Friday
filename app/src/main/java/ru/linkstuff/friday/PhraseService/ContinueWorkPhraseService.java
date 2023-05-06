package ru.linkstuff.friday.PhraseService;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import ru.linkstuff.friday.Friday;
import ru.linkstuff.friday.NewsFeed;
import ru.linkstuff.friday.R;
import ru.yandex.speechkit.PhraseSpotter;

public class ContinueWorkPhraseService extends Service{
    private static final String API_KEY = "6f29195e-0ac8-4979-ad8f-b70398be65b8";
    public static NotificationManager notification;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        notification = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotify();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
                startService(new Intent(context, PhraseService.class));
            }
        }, 5000);

        return START_NOT_STICKY;
    }

    private void showNotify(){
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.friday_notify_icon)
                .setContentTitle("Это оповещение скоро закроется...")
                .setContentText(getResources().getString(R.string.phraseservice_message_serviceworks))
                .setAutoCancel(false)
                .setOngoing(true);

        Intent intent = new Intent(context, NewsFeed.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(Friday.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        nBuilder.setContentIntent(pendingIntent);

        notification.notify(1, nBuilder.build());
    }

    public static void stop() {
        PhraseSpotter.stop();
        notification.cancelAll();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
