package com.example.tcc.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.example.tcc.R;
import com.example.tcc.service.MonitoringService;

public class Monitoring {

	private final View view;
	private final Context context;
	private final Activity activity;
	private final @IdRes int textViewId;
	private final Notification notification;
	private final PowerManager.WakeLock wakeLock;

	private TextView textView;

	private final Handler textViewHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message message) {
			if ((textView == null) && (view != null)) {
				textView = view.findViewById(textViewId);
			}

			if ((textView != null) && (message.obj != null)) {
				textView.setText(String.valueOf(message.obj));
			}
		}
	};

	private BroadcastReceiver batteryReceiver;
	private boolean isBatteryReceiverRegistered = false;

	public Monitoring(View view, Context context, Activity activity, @IdRes int textViewId) {
		this.view = view;
		this.context = context;
		this.activity = activity;
		this.textViewId = textViewId;
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
		int timeout = sharedPreferences.getInt(getContext().getString(R.string.preference_key_timeLimitForMonitoring), getContext().getResources().getInteger(R.integer.preference_defaultValue_timeLimitForMonitoring));

		wakeLock.acquire(timeout);

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
		monitoringServiceIntent.putExtra("textViewMessenger", new Messenger(textViewHandler));
		monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_decibelsForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_decibelsForPositiveReading));
		monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_timeForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_timeForPositiveReading));

		getContext().startForegroundService(monitoringServiceIntent);
	}

	public void stop() {
		if (isForegroundServiceRunning()) {
			Intent monitoringServiceIntent = new Intent(getContext(), MonitoringService.class);

			monitoringServiceIntent.setAction(MonitoringService.ACTION_STOP);
			monitoringServiceIntent.putExtra("textViewMessenger", new Messenger(textViewHandler));
			monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_decibelsForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_decibelsForPositiveReading));
			monitoringServiceIntent.putExtra(getContext().getString(R.string.preference_key_timeForPositiveReading), getContext().getResources().getInteger(R.integer.preference_defaultValue_timeForPositiveReading));

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

	private boolean isForegroundServiceRunning() {
		ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);

		for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (MonitoringService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}

		return false;
	}

}