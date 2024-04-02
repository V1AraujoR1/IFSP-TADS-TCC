package com.example.tcc.service;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.tcc.R;
import com.example.tcc.util.TimeElapsed;

import java.util.Objects;

public class MonitoringService extends Service {

	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	private static final int SAMPLE_RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private static final int NOTIFICATION_ID = 1;
	private static final long MILLISECONDS = 200;
	private final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
	private TimeElapsed timeElapsed;
	private AudioRecord audioRecord;
	private long decibelThreshold;
	private long timeForPositiveReading;
	private long timeLimitForMonitoring;
	private final long startTime = SystemClock.elapsedRealtime();
	private Handler handler;
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			short[] buffer = new short[minBufferSize];
			int numSamplesRead = audioRecord.read(buffer, 0, buffer.length);
			double sumOfSquares = 0;

			for (int i = 0; i < numSamplesRead; i++) {
				sumOfSquares += buffer[i] * buffer[i];
			}

			double amplitude = sumOfSquares / (double) numSamplesRead;
			long decibel = Math.round(20 * Math.log10(Math.sqrt(amplitude)));

			if (decibel > decibelThreshold) {
				timeElapsed.start();
			} else {
				timeElapsed.stop();
			}

			updateNotification(decibel);

			if (getElapsedTime() >= timeLimitForMonitoring) {
				stopMonitoringService();
				return;
			}

			handler.postDelayed(this, MILLISECONDS);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		decibelThreshold = intent.getIntExtra(getString(R.string.preference_key_decibelsForPositiveReading), MonitoringService.this.getResources().getInteger(R.integer.preference_defaultValue_decibelsForPositiveReading));
		timeForPositiveReading = intent.getIntExtra(getString(R.string.preference_key_timeForPositiveReading), MonitoringService.this.getResources().getInteger(R.integer.preference_defaultValue_timeForPositiveReading));
		timeLimitForMonitoring = intent.getIntExtra(getString(R.string.preference_key_timeLimitForMonitoring), MonitoringService.this.getResources().getInteger(R.integer.preference_defaultValue_timeLimitForMonitoring));

		if (Objects.equals(intent.getAction(), ACTION_STOP)) {
			stopMonitoringService();
			return START_NOT_STICKY;
		}

		startMonitoringService();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@NonNull
	private android.app.Notification createNotification(long decibel) {
		return new NotificationCompat.Builder(this, getString(R.string.monitoringService_notification_channelId))
				.setSmallIcon(R.mipmap.ic_babyassist)
				.setContentTitle(getString(R.string.monitoringService_notification_contentTitle))
				.setContentText(getString(R.string.monitoringService_notification_contentText, String.valueOf(decibel)))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(false)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
				.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
				.build();
	}

	private void updateNotification(long decibel) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, createNotification(decibel));
	}

	private void startMonitoringService() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			stopMonitoringService();
			return;
		}

		startForeground(NOTIFICATION_ID, createNotification(0), FOREGROUND_SERVICE_TYPE_MICROPHONE);

		timeElapsed = new TimeElapsed(this, timeForPositiveReading);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
		audioRecord.startRecording();

		handler = new Handler(Looper.getMainLooper());
		handler.post(runnable);
	}

	private void stopMonitoringService() {
		if (timeElapsed != null) {
			timeElapsed.stop();
		}

		if ((audioRecord != null) && (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
			audioRecord.stop();
			audioRecord.release();
		}

		if (handler != null) {
			handler.removeCallbacks(runnable);
		}

		stopForeground(STOP_FOREGROUND_REMOVE);
		stopSelf();
	}

	private long getElapsedTime() {
		return SystemClock.elapsedRealtime() - startTime;
	}

}