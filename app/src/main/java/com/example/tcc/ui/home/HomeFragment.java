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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tcc.R;
import com.example.tcc.TimeElapsed;
import com.example.tcc.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

	private static final int SAMPLE_RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RECORD_AUDIO_REQUEST_CODE = 123;
	private static final long DECIBEL_THRESHOLD = 60;

	private AudioRecord gcAudioRecord;
	private Thread gcRecordingThread;

	private FragmentHomeBinding binding;

	private ToggleButton toggleButton;
	private Context appContext;

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		toggleButton = root.findViewById(R.id.tglMonitoring);

		toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToggleButton tglMonitoring = (ToggleButton) v;

				if (tglMonitoring.isChecked()) {
					boolean bSuccessfulStart = startMonitoring();

					if (!bSuccessfulStart) {
						tglMonitoring.setChecked(false);
					}
				} else {
					stopMonitoring();
				}
			}
		});

		return root;
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