package com.example.tcc.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.PowerManager;
import androidx.preference.PreferenceManager;

import com.example.tcc.R;
import com.example.tcc.service.MonitoringService;

public class Monitoring {

	private final Context context;
	private final Activity activity;
	private final Notification notification;
	private final PowerManager.WakeLock wakeLock;

	private BroadcastReceiver batteryReceiver;
	private boolean isBatteryReceiverRegistered = false;

	public Monitoring(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		this.notification = new Notification(getContext());

		PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
		this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TCC:MonitoringWakeLock");
	}

	public Context getContext() {
		return context;
	}

	public Activity getActivity() {
		return activity;
	}

	public void start() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		int decibelsForPositiveReading = sharedPreferences.getInt(getContext().getString(R.string.preference_key_decibelsForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_decibelsForPositiveReading));
		int timeForPositiveReading = sharedPreferences.getInt(getContext().getString(R.string.preference_key_timeForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_timeForPositiveReading));
		int timeLimitForMonitoring = sharedPreferences.getInt(getContext().getString(R.string.preference_key_timeLimitForMonitoring), getContext().getResources().getInteger(R.integer.preference_defaultValue_timeLimitForMonitoring));

		wakeLock.acquire(timeLimitForMonitoring);

		batteryReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				notification.notifyLowBatteryLevel();
			}
		};

		getActivity().registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
		isBatteryReceiverRegistered = true;

		Intent monitoringServiceIntent = new Intent(getContext(), MonitoringService.class);

		monitoringServiceIntent.setAction(MonitoringService.ACTION_START);
		monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_decibelsForPositiveReading), decibelsForPositiveReading);
		monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_timeForPositiveReading), timeForPositiveReading);
		monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_timeLimitForMonitoring), timeLimitForMonitoring);

		getContext().startForegroundService(monitoringServiceIntent);
	}

	public void stop() {
		if (isForegroundServiceRunning()) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

			int decibelsForPositiveReading = sharedPreferences.getInt(getContext().getString(R.string.preference_key_decibelsForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_decibelsForPositiveReading));
			int timeForPositiveReading = sharedPreferences.getInt(getContext().getString(R.string.preference_key_timeForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_timeForPositiveReading));
			int timeLimitForMonitoring = sharedPreferences.getInt(getContext().getString(R.string.preference_key_timeLimitForMonitoring), getContext().getResources().getInteger(R.integer.preference_defaultValue_timeLimitForMonitoring));

			Intent monitoringServiceIntent = new Intent(getContext(), MonitoringService.class);

			monitoringServiceIntent.setAction(MonitoringService.ACTION_STOP);
			monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_decibelsForPositiveReading), decibelsForPositiveReading);
			monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_timeForPositiveReading), timeForPositiveReading);
			monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_timeLimitForMonitoring), timeLimitForMonitoring);

			getContext().startForegroundService(monitoringServiceIntent);
		}

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}

		if (isBatteryReceiverRegistered) {
			getActivity().unregisterReceiver(batteryReceiver);
			isBatteryReceiverRegistered = false;
		}
	}

	public boolean isForegroundServiceRunning() {
		ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);

		for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (MonitoringService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}

		return false;
	}

}