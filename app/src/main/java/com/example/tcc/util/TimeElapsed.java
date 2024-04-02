package com.example.tcc.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.tcc.R;

public class TimeElapsed {
	private final Context context;
	private final long milliseconds;
	private final Notification notification;
	private final Handler handler;
	private final Runnable runnable;

	private boolean isStarted = false;

	public TimeElapsed(Context context, long milliseconds) {
		this.context = context;
		this.milliseconds = milliseconds;
		this.notification = new Notification(getContext());
		this.handler = new Handler(Looper.getMainLooper());
		this.runnable = () -> {
			Toast.makeText(getContext(), getContext().getString(R.string.temporaryMessageForDemonstrationPurposes), Toast.LENGTH_LONG).show();
			notification.notifyPositiveReadingFromMonitoring();
			isStarted = false;
		};
	}

	public void start() {
		if (isStarted) {
			return;
		}

		isStarted = true;
		handler.postDelayed(runnable, milliseconds);
	}

	public void stop() {
		if (!isStarted) {
			return;
		}

		isStarted = false;
		handler.removeCallbacks(runnable);
	}

	public Context getContext() {
		return context;
	}
}