package com.example.tcc.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.tcc.R;

public class SettingsFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey);

		Preference phoneNumbersPreference = findPreference(getString(R.string.preference_key_notificationRecipients));

		if (phoneNumbersPreference != null) {
			phoneNumbersPreference.setOnPreferenceClickListener(preference -> {
				NavHostFragment.findNavController(SettingsFragment.this).navigate(R.id.action_settingsFragment_to_notificationRecipientsFragment);
				return true;
			});
		}
	}

	@Override
	public void onDisplayPreferenceDialog(@NonNull Preference preference) {
		if (preference instanceof NumberPickerDialogPreference) {
			DialogFragment dialogFragment = NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
			dialogFragment.setTargetFragment(this, 0);

			if (isAdded()) {
				dialogFragment.show(getParentFragmentManager(), "NumberPickerDialogPreference");
			}
		} else if (preference instanceof TimePickerDialogPreference) {
			DialogFragment dialogFragment = TimePickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
			dialogFragment.setTargetFragment(this, 0);

			if (isAdded()) {
				dialogFragment.show(getParentFragmentManager(), "TimePickerDialogPreference");
			}
		} else {
			super.onDisplayPreferenceDialog(preference);
		}
	}

}