package com.example.news_application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CharSequence name = "default_channel";
        String description = "Default notification channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("default_channel", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(ApiWorker.class, 5, TimeUnit.SECONDS)
                .setInitialDelay(3, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(periodicWorkRequest);
    }
}
