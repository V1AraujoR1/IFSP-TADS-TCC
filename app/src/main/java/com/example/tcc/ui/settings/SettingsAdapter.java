package com.example.tcc.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tcc.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public final class SettingsAdapter extends RecyclerView.Adapter {
	private final Context context;
	private final SettingsViewModel settingsViewModel;

	public SettingsAdapter(@NonNull Context context) {
		super();
		this.context = context;
		this.settingsViewModel = new SettingsViewModel(this.context);
	}

	public final Context getContext() {
		return this.context;
	}

	@Override
	@NonNull
	public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		View view = layoutInflater.inflate(R.layout.list_item_phone_number, parent, false);
		return new SettingsViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		onBindViewHolder((SettingsViewHolder) holder, position);
	}

	public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
		holder.textViewPhoneNumber.setText(this.getSettingsViewModel().getPhoneNumbers().get(position));
	}

	@Override
	public int getItemCount() {
		return this.getSettingsViewModel().getPhoneNumbers().size();
	}

	public SettingsViewModel getSettingsViewModel() {
		return settingsViewModel;
	}

	public final class SettingsViewHolder extends RecyclerView.ViewHolder {
		private final View v;
		private TextView textViewPhoneNumber;
		private ImageView iv;

		public SettingsViewHolder(@NonNull View itemView) {
			super(itemView);
			this.v = itemView;

			this.textViewPhoneNumber = this.v.findViewById(R.id.textViewPhoneNumber);
			this.iv = this.v.findViewById(R.id.mMenus);

			this.iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					popupMenu(v);
				}
			});
		}

		private void popupMenu(View v) {
			final String position = SettingsAdapter.this.getSettingsViewModel().getPhoneNumbers().get(this.getAdapterPosition());
			PopupMenu popupMenus = new PopupMenu(SettingsAdapter.this.getContext(), v);
			popupMenus.inflate(R.menu.show_menu);
			popupMenus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {

					if (item.getItemId() == R.id.editText) {
						View view = LayoutInflater.from(SettingsAdapter.this.getContext()).inflate(R.layout.add_item, null);
						EditText number = view.findViewById(R.id.userNo);
						new AlertDialog.Builder(SettingsAdapter.this.getContext()).setView(view).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SettingsAdapter.this.getSettingsViewModel().getPhoneNumbers().set(SettingsViewHolder.this.getAdapterPosition(), number.getText().toString());
								SettingsAdapter.this.notifyDataSetChanged();
								SettingsAdapter.this.getSettingsViewModel().saveSettings();
								dialog.dismiss();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create().show();
					}

					if (item.getItemId() == R.id.delete) {
						new AlertDialog.Builder(SettingsAdapter.this.getContext()).setTitle(R.string.delete_confirmation_title).setIcon(R.drawable.ic_warning).setMessage(R.string.delete_confirmation_message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SettingsAdapter.this.getSettingsViewModel().getPhoneNumbers().remove(SettingsViewHolder.this.getAdapterPosition());
								SettingsAdapter.this.notifyDataSetChanged();
								SettingsAdapter.this.getSettingsViewModel().saveSettings();
								dialog.dismiss();
							}
						}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create().show();
					}

					return true;
				}
			});
			popupMenus.show();
			try {
				Field popup = PopupMenu.class.getDeclaredField("mPopup");
				popup.setAccessible(true);
				Object menu = popup.get(popupMenus);
				menu.getClass().getDeclaredMethod("setForceShowIcon", boolean.class).invoke(menu, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}
}