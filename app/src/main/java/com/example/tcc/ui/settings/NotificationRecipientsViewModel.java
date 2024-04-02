package com.example.tcc.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.tcc.R;

import java.util.ArrayList;
import java.util.Arrays;

public class NotificationRecipientsViewModel {
	private final Context context;
	private final SharedPreferences sharedPreferences;
	private final ArrayList<String> phoneNumbers;

	public NotificationRecipientsViewModel(Context context) {
		this.context = context;
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		this.phoneNumbers = new ArrayList<>();

		loadSettings();
	}

	public void loadSettings() {
		String phoneNumbersString = sharedPreferences.getString(getContext().getString(R.string.preference_key_notificationPhoneNumbers), "");

		this.getPhoneNumbers().clear();

		if (!phoneNumbersString.isEmpty()) {
			String[] phoneNumberArray = phoneNumbersString.split(",");
			this.getPhoneNumbers().addAll(Arrays.asList(phoneNumberArray));
		}
	}

	public void saveSettings() {
		StringBuilder stringBuilder = new StringBuilder();

		phoneNumbers.forEach(phoneNumber -> stringBuilder.append(phoneNumber).append(","));

		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(getContext().getString(R.string.preference_key_notificationPhoneNumbers), stringBuilder.toString());
		editor.apply();
	}

	public Context getContext() {
		return context;
	}

	public ArrayList<String> getPhoneNumbers() {
		return phoneNumbers;
	}
}