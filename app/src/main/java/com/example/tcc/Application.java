package com.example.tcc;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.example.tcc.service.MonitoringService;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();

		NotificationManager notificationManager = getSystemService(NotificationManager.class);
		NotificationChannel notificationChannel = notificationManager.getNotificationChannel(getString(R.string.monitoringService_notification_channelId));

		if (notificationChannel == null) {
			notificationChannel = new NotificationChannel(getString(R.string.monitoringService_notification_channelId), getString(R.string.monitoringService_notification_channelName), NotificationManager.IMPORTANCE_HIGH);
			notificationManager.createNotificationChannel(notificationChannel);
		}
	}

}