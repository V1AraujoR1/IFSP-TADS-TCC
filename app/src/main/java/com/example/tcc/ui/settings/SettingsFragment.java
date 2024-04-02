package com.example.tcc.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.tcc.databinding.FragmentSettingsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SettingsFragment extends Fragment {

	private FragmentSettingsBinding binding;

	private RecyclerView recyclerView;
	private Context fragmentContext;
	private SettingsAdapter adapter;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentSettingsBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		return root;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		recyclerView = view.findViewById(R.id.recyclerView);
		fragmentContext = requireContext();
		adapter = new SettingsAdapter(fragmentContext);

		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

		FloatingActionButton fab = view.findViewById(R.id.addingBtn);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAlertDialog();
			}
		});
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

		addDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String number = userNo.getText().toString();
				adapter.getSettingsViewModel().getPhoneNumbers().add(number);
				adapter.notifyDataSetChanged();
				adapter.getSettingsViewModel().saveSettings();
				dialog.dismiss();
			}
		});

		addDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		addDialog.create().show();
	}
}