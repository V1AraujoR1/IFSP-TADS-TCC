package com.example.tcc.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.tcc.R;
import com.example.tcc.databinding.FragmentHomeBinding;
import com.example.tcc.util.Monitoring;

import java.util.ArrayList;
public class HomeFragment extends Fragment {

	private FragmentHomeBinding binding;
	private Monitoring monitoring;
	private ToggleButton toggleButton;
	private Handler handler;
	private Runnable runnable;

	private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
		if (isGranted) {
			tryStartMonitoring();
		} else {
			stopMonitoring();
		}
	});

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		ensureButtonState();
	}

	@Override
	public void onResume() {
		super.onResume();
		ensureButtonState();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		monitoring = new Monitoring(requireContext().getApplicationContext(), getActivity());
		toggleButton = view.findViewById(R.id.tglMonitoring);

		toggleButton.setOnClickListener((v) -> {
			if (toggleButton.isChecked()) {
				tryStartMonitoring();
			} else {
				stopMonitoring();
			}
		});

		ensureButtonState();
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		ensureButtonState();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void tryStartMonitoring() {
		ArrayList<String> permissionsArrayList = new ArrayList<>();

		try {
			if ((requireContext().getPackageManager().getPermissionInfo(Manifest.permission.FOREGROUND_SERVICE, 0).getProtection() == PermissionInfo.PROTECTION_DANGEROUS) && (ActivityCompat.checkSelfPermission(requireContext().getApplicationContext(), Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)) {
				permissionsArrayList.add(Manifest.permission.FOREGROUND_SERVICE);
			}

			if ((requireContext().getPackageManager().getPermissionInfo(Manifest.permission.POST_NOTIFICATIONS, 0).getProtection() == PermissionInfo.PROTECTION_DANGEROUS) && (ActivityCompat.checkSelfPermission(requireContext().getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
				permissionsArrayList.add(Manifest.permission.POST_NOTIFICATIONS);
			}

			if ((requireContext().getPackageManager().getPermissionInfo(Manifest.permission.RECORD_AUDIO, 0).getProtection() == PermissionInfo.PROTECTION_DANGEROUS) && (ActivityCompat.checkSelfPermission(requireContext().getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
				permissionsArrayList.add(Manifest.permission.RECORD_AUDIO);
			}

			if ((requireContext().getPackageManager().getPermissionInfo(Manifest.permission.SEND_SMS, 0).getProtection() == PermissionInfo.PROTECTION_DANGEROUS) && (ActivityCompat.checkSelfPermission(requireContext().getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)) {
				permissionsArrayList.add(Manifest.permission.SEND_SMS);
			}

			if ((requireContext().getPackageManager().getPermissionInfo(Manifest.permission.WAKE_LOCK, 0).getProtection() == PermissionInfo.PROTECTION_DANGEROUS) && (ActivityCompat.checkSelfPermission(requireContext().getApplicationContext(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED)) {
				permissionsArrayList.add(Manifest.permission.WAKE_LOCK);
			}
		} catch (Exception ignored) {
		}

		if (permissionsArrayList.isEmpty()) {
			startMonitoring();
		} else {
			requestPermissionLauncher.launch(permissionsArrayList.get(0));
		}
	}

	private void startMonitoring() {
		toggleButton.setChecked(true);
		monitoring.start();
	}

	private void stopMonitoring() {
		toggleButton.setChecked(false);
		monitoring.stop();
	}

	private void ensureButtonState() {
		if ((handler != null) && (runnable != null)) {
			return;
		}

		handler = new Handler(Looper.getMainLooper());
		runnable = new Runnable() {
			@Override
			public void run() {
				updateButtonState();
				handler.postDelayed(this, 1000);
			}
		};

		handler.post(runnable);
	}

	private void updateButtonState() {
		if ((monitoring == null) || (toggleButton == null)) {
			return;
		}

		if ((toggleButton.isChecked()) && (!monitoring.isForegroundServiceRunning())) {
			toggleButton.setChecked(false);
		}

		if ((!toggleButton.isChecked()) && (monitoring.isForegroundServiceRunning())) {
			toggleButton.setChecked(true);
		}
	}
}