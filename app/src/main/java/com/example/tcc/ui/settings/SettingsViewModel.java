package com.example.tcc.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsViewModel {
	private Context context;
	private ArrayList<String> phoneNumbers;

	public SettingsViewModel(Context context) {
		this.context = context;
		this.phoneNumbers = new ArrayList<String>();

		loadSettings();
	}

	public void loadSettings() {
		SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
		String phoneNumbersString = sharedPreferences.getString("phoneNumbers", "");

		this.getPhoneNumbers().clear();

		if (!phoneNumbersString.isEmpty()) {
			String[] phoneNumberArray = phoneNumbersString.split(",");
			this.getPhoneNumbers().addAll(Arrays.asList(phoneNumberArray));
		}
	}

	public void saveSettings() {
		SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		StringBuilder stringBuilder = new StringBuilder();
		for (String phoneNumber : phoneNumbers) {
			stringBuilder.append(phoneNumber).append(",");
		}
		String phoneNumbersString = stringBuilder.toString();

		editor.putString("phoneNumbers", phoneNumbersString);
		editor.apply();
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public ArrayList<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
}
