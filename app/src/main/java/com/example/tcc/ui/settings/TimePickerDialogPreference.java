package com.example.tcc.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import com.example.tcc.R;

import java.time.LocalTime;

public class TimePickerDialogPreference extends DialogPreference {

	private int defaultValue;
	private int value;

	public TimePickerDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
		persistInt(getValue());
	}

	@Override
	public int getDialogLayoutResource() {
		return R.layout.preference_dialog_timepicker;
	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		super.setDefaultValue(defaultValue);
		this.defaultValue = (int) defaultValue;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		defaultValue = a.getInt(index, defaultValue);
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(@Nullable Object defaultValue) {
		int value = 0;

		if ((defaultValue instanceof Integer)) {
			value = (int) defaultValue;
		}

		value = getPersistedInt(value);

		setValue(value);

		super.onSetInitialValue(value);
	}

	public LocalTime getLocalTime() {
		return getLocalTime(getValue());
	}

	public LocalTime getLocalTime(int milliseconds) {
		int hour = milliseconds / 1000 / 60 / 60;
		int minute = (milliseconds - hour * 60 * 60 * 1000) / 1000 / 60;
		int second = (milliseconds - (hour * 60 * 60 * 1000) - (minute * 60 * 1000)) / 1000;

		return LocalTime.of(hour, minute, second);
	}

	public int getMilliseconds(@NonNull LocalTime localTime) {
		return (localTime.getHour() * 60 * 60 * 1000) + (localTime.getMinute() * 60 * 1000) + (localTime.getSecond() * 1000);
	}

}