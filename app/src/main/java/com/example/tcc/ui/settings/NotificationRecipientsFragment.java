package com.example.tcc.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tcc.R;
import com.example.tcc.databinding.FragmentNotificationRecipientsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotificationRecipientsFragment extends Fragment {

	private FragmentNotificationRecipientsBinding binding;
	private Context fragmentContext;
	private NotificationRecipientsAdapter adapter;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentNotificationRecipientsBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		fragmentContext = requireContext();
		adapter = new NotificationRecipientsAdapter(fragmentContext);

		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

		FloatingActionButton fab = view.findViewById(R.id.addingBtn);

		fab.setOnClickListener((v) -> showAlertDialog());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void showAlertDialog() {
		AlertDialog.Builder addDialog = new AlertDialog.Builder(fragmentContext);

		View v = LayoutInflater.from(fragmentContext).inflate(R.layout.add_item, null);

		addDialog.setView(v);
		EditText userNo = v.findViewById(R.id.userNo);

		addDialog.setPositiveButton(R.string.confirm, (dialog, which) -> {
			String number = userNo.getText().toString();
			adapter.getNotificationRecipientsViewModel().getPhoneNumbers().add(number);
			adapter.notifyDataSetChanged();
			adapter.getNotificationRecipientsViewModel().saveSettings();
			dialog.dismiss();
		});

		addDialog.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
		addDialog.create().show();
	}
}