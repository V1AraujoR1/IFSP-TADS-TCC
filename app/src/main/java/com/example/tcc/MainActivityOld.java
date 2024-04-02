package com.example.tcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivityOld extends AppCompatActivity {

	//region Constants (Private)
	private static final int SAMPLE_RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RECORD_AUDIO_REQUEST_CODE = 123;
	private static final long DECIBEL_THRESHOLD = 60;
	//endregion

	//region Variables (Private)
	private AudioRecord gcAudioRecord;
	private Thread gcRecordingThread;
	//endregion

	//region Methods (Protected) (Override)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_old);
	}
	//endregion

	//region Methods (Private)
	private boolean startMonitoring() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
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
				TimeElapsed timeElapsed = new TimeElapsed(getApplicationContext());
				TextView lblDecibel = findViewById(R.id.lblDecibel);
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
	//endregion

	//region Events (Public) (Override)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
			if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
				((ToggleButton) findViewById(R.id.tglMonitoring)).setChecked(true);
				startMonitoring();
			}
		}
	}
	//endregion

	//region Events (Public)
	public void tglMonitoring_onClick(View v) {
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
	//endregion
}