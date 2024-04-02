package com.example.tcc.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.example.tcc.R;

import java.time.LocalTime;

public class TimePickerPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

	private NumberPicker numberPickerHours;
	private NumberPicker numberPickerMinutes;
	private NumberPicker numberPickerSeconds;

	@NonNull
	public static TimePickerPreferenceDialogFragmentCompat newInstance(String key) {
		final TimePickerPreferenceDialogFragmentCompat fragment = new TimePickerPreferenceDialogFragmentCompat();
		final Bundle b = new Bundle(1);
		b.putString(ARG_KEY, key);
		fragment.setArguments(b);

		return fragment;
	}

	@Override
	protected void onBindDialogView(@NonNull View view) {
		super.onBindDialogView(view);

		numberPickerHours = view.findViewById(R.id.hours);
		numberPickerMinutes = view.findViewById(R.id.minutes);
		numberPickerSeconds = view.findViewById(R.id.seconds);

		numberPickerHours.setMinValue(0);
		numberPickerHours.setMaxValue(23);

		numberPickerMinutes.setMinValue(0);
		numberPickerMinutes.setMaxValue(59);

		numberPickerSeconds.setMinValue(0);
		numberPickerSeconds.setMaxValue(59);

		DialogPreference preference = getPreference();
		LocalTime localTime = null;

		if (preference instanceof TimePickerDialogPreference) {
			localTime = ((TimePickerDialogPreference) preference).getLocalTime();
		}

		if (localTime != null) {
			numberPickerHours.setValue(localTime.getHour());
			numberPickerMinutes.setValue(localTime.getMinute());
			numberPickerSeconds.setValue(localTime.getSecond());
		}
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			DialogPreference preference = getPreference();

			if (preference instanceof TimePickerDialogPreference) {
				TimePickerDialogPreference timePickerDialogPreference = (TimePickerDialogPreference) preference;

				int hour = numberPickerHours.getValue();
				int minute = numberPickerMinutes.getValue();
				int second = numberPickerSeconds.getValue();

				int newValue = timePickerDialogPreference.getMilliseconds(LocalTime.of(hour, minute, second));

				if (timePickerDialogPreference.callChangeListener(newValue)) {
					timePickerDialogPreference.setValue(newValue);
				}
			}
		}
	}

}