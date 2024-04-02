package com.example.tcc.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NumberPickerPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

	private NumberPicker numberPicker;

	@NonNull
	public static NumberPickerPreferenceDialogFragmentCompat newInstance(String key) {
		final NumberPickerPreferenceDialogFragmentCompat fragment = new NumberPickerPreferenceDialogFragmentCompat();
		final Bundle b = new Bundle(1);
		b.putString(ARG_KEY, key);
		fragment.setArguments(b);

		return fragment;
	}

	@Nullable
	@Override
	protected View onCreateDialogView(@NonNull Context context) {
		numberPicker = new NumberPicker(context);
		numberPicker.setScrollBarSize(0);
		numberPicker.setMinValue(0);
		numberPicker.setMaxValue(194);

		return numberPicker;
	}

	@Override
	protected void onBindDialogView(@NonNull View view) {
		super.onBindDialogView(view);

		DialogPreference preference = getPreference();
		Integer value = null;

		if (preference instanceof NumberPickerDialogPreference) {
			value = ((NumberPickerDialogPreference) preference).getValue();
		}

		if (value != null) {
			numberPicker.setValue(value);
		}
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			DialogPreference preference = getPreference();

			if (preference instanceof NumberPickerDialogPreference) {
				NumberPickerDialogPreference numberPickerDialogPreference = (NumberPickerDialogPreference) preference;
				int newValue = numberPicker.getValue();

				if (numberPickerDialogPreference.callChangeListener(newValue)) {
					numberPickerDialogPreference.setValue(newValue);
				}
			}
		}
	}

}