package com.example.tcc.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.telephony.SmsManager;

import com.example.tcc.R;
import com.example.tcc.ui.settings.NotificationRecipientsViewModel;

public class TimeElapsed {
	private final Context context;
	private final NotificationRecipientsViewModel notificationRecipientsViewModel;
	private final Handler handler;
	private final Runnable runnable;
	private final long milliseconds;

	private boolean isStarted = false;

	public TimeElapsed(Context context) {
		this.context = context;
		this.notificationRecipientsViewModel = new NotificationRecipientsViewModel(getContext());
		this.handler = new Handler(Looper.getMainLooper());
		this.runnable = new Runnable() {
			@Override
			public void run() {
				TimeElapsed.this.run();
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

	private void run() {
		try {
			Toast.makeText(getContext(), getContext().getString(R.string.temporaryMessageForDemonstrationPurposes), Toast.LENGTH_LONG).show();

			if ((notificationRecipientsViewModel.getPhoneNumbers() != null) && (!notificationRecipientsViewModel.getPhoneNumbers().isEmpty())) {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(notificationRecipientsViewModel.getPhoneNumbers().get(0), null, "SMS de teste para o projeto do TCC.", null, null);
			}
		} catch (Exception e) {
			String testingMessage = e.getMessage();
		} finally {
			isStarted = false;
		}
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