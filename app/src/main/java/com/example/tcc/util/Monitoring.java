package com.example.tcc.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Looper;
import android.os.Process;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.app.ActivityCompat;

import com.example.tcc.R;

public class Monitoring {

	private static final int SAMPLE_RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RECORD_AUDIO_REQUEST_CODE = 123;
	private static final int SEND_SMS_REQUEST_CODE = 124;
	private static final long DECIBEL_THRESHOLD = 60;

	private final Context context;
	private final Activity activity;
	private final @IdRes int textViewId;

	private View view;
	private TextView textView;
	private AudioRecord audioRecord;
	private Thread thread;

	public Monitoring(View view, Context context, Activity activity, @IdRes int textViewId) {
		this.view = view;
		this.context = context;
		this.activity = activity;
		this.textViewId = textViewId;
	}

	public boolean start() {
		if (textView == null) {
			textView = view.findViewById(this.textViewId);
		}

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
			return false;
		}

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.SEND_SMS}, SEND_SMS_REQUEST_CODE);
			return false;
		}

		int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
		audioRecord.startRecording();

		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

				Looper.prepare();
				TimeElapsed timeElapsed = new TimeElapsed(context);
				short[] buffer = new short[minBufferSize];

				try {
					while (true) {
						Thread.sleep(200);

						int numSamplesRead = audioRecord.read(buffer, 0, buffer.length);
						double sumOfSquares = 0;

						for (int i = 0; i < numSamplesRead; i++) {
							sumOfSquares += buffer[i] * buffer[i];
						}

						double amplitude = sumOfSquares / (double) numSamplesRead;
						long decibel = Math.round(20 * Math.log10(Math.sqrt(amplitude)));

						if (textView != null) {
							textView.post(new Runnable() {
								@Override
								public void run() {
									textView.setText(context.getString(R.string.lblDecibelMessage, String.valueOf(decibel)));
								}
							});
						}

						if (decibel > DECIBEL_THRESHOLD) {
							timeElapsed.start();
						} else {
							timeElapsed.stop();
						}
					}
				} catch (Exception e) {
					timeElapsed.stop();

					if ((audioRecord != null) && (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
						audioRecord.stop();
						audioRecord.release();
					}

					if (textView != null) {
						textView.post(new Runnable() {
							@Override
							public void run() {
								textView.setText("");
							}
						});
					}
				}
			}
		});

		thread.start();
		return true;
	}

	public void stop() {
		if (thread != null) {
			thread.interrupt();
		}
	}

}