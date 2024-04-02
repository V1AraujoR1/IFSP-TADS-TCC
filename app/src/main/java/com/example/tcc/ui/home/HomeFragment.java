package com.example.tcc.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.tcc.R;
import com.example.tcc.util.Monitoring;
import com.example.tcc.util.TimeElapsed;
import com.example.tcc.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

	private static final int SAMPLE_RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RECORD_AUDIO_REQUEST_CODE = 123;
	private static final int SEND_SMS_REQUEST_CODE = 124;
	private static final long DECIBEL_THRESHOLD = 60;

	private AudioRecord gcAudioRecord;
	private Thread gcRecordingThread;

	private FragmentHomeBinding binding;

	private ToggleButton toggleButton;
	private Context appContext;

	private Monitoring monitoring;

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		return root;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		toggleButton = view.findViewById(R.id.tglMonitoring);

		monitoring = new Monitoring(view, requireContext().getApplicationContext(), getActivity(), R.id.lblDecibel);

		toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToggleButton tglMonitoring = (ToggleButton) v;

				/*if (tglMonitoring.isChecked()) {
					boolean bSuccessfulStart = startMonitoring();

					if (!bSuccessfulStart) {
						tglMonitoring.setChecked(false);
					}
				} else {
					stopMonitoring();
				}*/

				if (tglMonitoring.isChecked()) {
					boolean bSuccessfulStart = monitoring.start();

					if (!bSuccessfulStart) {
						tglMonitoring.setChecked(false);
					}
				} else {
					monitoring.stop();
				}
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private boolean startMonitoring() {
		appContext = requireContext().getApplicationContext();

		if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
			return false;
		}

		if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.SEND_SMS}, SEND_SMS_REQUEST_CODE);
			return false;
		}

		int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

		gcAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
		gcAudioRecord.startRecording();

		gcRecordingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

				Looper.prepare();
				TimeElapsed timeElapsed = new TimeElapsed(appContext);
				TextView lblDecibel = getView().findViewById(R.id.lblDecibel);
				short[] buffer = new short[minBufferSize];

				try {
					while (true) {
						Thread.sleep(200);

						int numSamplesRead = gcAudioRecord.read(buffer, 0, buffer.length);
						double sumOfSquares = 0;

						for (int i = 0; i < numSamplesRead; i++) {
							sumOfSquares += buffer[i] * buffer[i];
						}

						double amplitude = sumOfSquares / (double) numSamplesRead;
						long decibel = Math.round(20 * Math.log10(Math.sqrt(amplitude)));

						lblDecibel.post(new Runnable() {
							@Override
							public void run() {
								lblDecibel.setText(getString(R.string.lblDecibelMessage, String.valueOf(decibel)));
							}
						});

						if (decibel > DECIBEL_THRESHOLD) {
							timeElapsed.start();
						} else {
							timeElapsed.stop();
						}
					}
				} catch (Exception e) {
					timeElapsed.stop();

					if ((gcAudioRecord != null) && (gcAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
						gcAudioRecord.stop();
						gcAudioRecord.release();
					}

					lblDecibel.post(new Runnable() {
						@Override
						public void run() {
							lblDecibel.setText("");
						}
					});
				}
			}
		});

		gcRecordingThread.start();
		return true;
	}

	private void stopMonitoring() {
		gcRecordingThread.interrupt();
	}
}