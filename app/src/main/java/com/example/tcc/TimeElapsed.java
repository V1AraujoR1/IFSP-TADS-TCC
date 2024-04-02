package com.example.tcc;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.content.Context;

public class TimeElapsed {
	private final Handler handler;
	private final Runnable runnable;
	private final long milliseconds;
	private boolean isStarted = false;

	public TimeElapsed(Context context) {
		this.handler = new Handler(Looper.getMainLooper());
		this.runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Toast.makeText(context, context.getString(R.string.temporaryMessageForDemonstrationPurposes), Toast.LENGTH_LONG).show();
				} catch (Exception e) {
				} finally {
					isStarted = false;
				}
			}
		};
		this.milliseconds = 3000;
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
}