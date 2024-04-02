package com.example.tcc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.tcc.R;
import com.example.tcc.ui.settings.NotificationRecipientsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Notification {
	private final Context context;
	private final SmsManager smsManager;
	private final SharedPreferences sharedPreferences;
	private final NotificationRecipientsViewModel notificationRecipientsViewModel;

	public Notification(Context context) {
		this.context = context;
		this.smsManager = SmsManager.getDefault();
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		this.notificationRecipientsViewModel = new NotificationRecipientsViewModel(getContext());
	}

	private void notify(String text) {
		try {
			Set<String> defaultTypes = new HashSet<>(Arrays.asList(getContext().getResources().getStringArray(R.array.preference_defaultValue_notificationTypes)));
			Set<String> selectedTypes = sharedPreferences.getStringSet(getContext().getString(R.string.preference_key_notificationTypes), defaultTypes);

			if (selectedTypes.contains("SMS")) {
				notifySMS(text);
			}
		} catch (Exception ignored) {
		}
	}

	private void notifySMS(String text) {
		Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
		ArrayList<String> phoneNumbers = notificationRecipientsViewModel.getPhoneNumbers();

		if ((phoneNumbers != null) && (!phoneNumbers.isEmpty())) {
			phoneNumbers.forEach((phoneNumber) -> smsManager.sendTextMessage(phoneNumber, null, text, null, null));
		}
	}

	public void notifyPositiveReadingFromMonitoring() {
		if (sharedPreferences.getBoolean(getContext().getString(R.string.preference_key_notifyPositiveReadingFromMonitoring), getContext().getResources().getBoolean(R.bool.preference_defaultValue_notifyPositiveReadingFromMonitoring))) {
			notify(sharedPreferences.getString(getContext().getString(R.string.preference_key_notificationMessage), getContext().getString(R.string.preference_defaultValue_notificationMessage)));
		}
	}

	public void notifyLowBatteryLevel() {
		if (sharedPreferences.getBoolean(getContext().getString(R.string.preference_key_notifyLowBatteryLevel), getContext().getResources().getBoolean(R.bool.preference_defaultValue_notifyLowBatteryLevel))) {
			notify(getContext().getString(R.string.preference_defaultValue_lowBatteryLevelNotificationMessage));
		}
	}

	private Context getContext() {
		return context;
	}
}