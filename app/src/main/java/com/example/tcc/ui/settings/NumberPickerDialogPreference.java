package com.example.tcc.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

public class NumberPickerDialogPreference extends DialogPreference {

	private int defaultValue;
	private int value;

	public NumberPickerDialogPreference(Context context, AttributeSet attrs) {
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

}