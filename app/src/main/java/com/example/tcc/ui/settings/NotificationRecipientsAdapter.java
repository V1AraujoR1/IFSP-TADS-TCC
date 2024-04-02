package com.example.tcc.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tcc.R;

import java.lang.reflect.Field;

public final class NotificationRecipientsAdapter extends RecyclerView.Adapter {
	private final Context context;
	private final NotificationRecipientsViewModel notificationRecipientsViewModel;

	public NotificationRecipientsAdapter(@NonNull Context context) {
		super();
		this.context = context;
		this.notificationRecipientsViewModel = new NotificationRecipientsViewModel(this.context);
	}

	public final Context getContext() {
		return this.context;
	}

	@Override
	@NonNull
	public NotificationRecipientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		View view = layoutInflater.inflate(R.layout.list_item_phone_number, parent, false);
		return new NotificationRecipientsViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		onBindViewHolder((NotificationRecipientsViewHolder) holder, position);
	}

	public void onBindViewHolder(@NonNull NotificationRecipientsViewHolder holder, int position) {
		holder.textViewPhoneNumber.setText(this.getNotificationRecipientsViewModel().getPhoneNumbers().get(position));
	}

	@Override
	public int getItemCount() {
		return this.getNotificationRecipientsViewModel().getPhoneNumbers().size();
	}

	public NotificationRecipientsViewModel getNotificationRecipientsViewModel() {
		return notificationRecipientsViewModel;
	}

	public final class NotificationRecipientsViewHolder extends RecyclerView.ViewHolder {
		private final View v;
		private TextView textViewPhoneNumber;
		private ImageView iv;

		public NotificationRecipientsViewHolder(@NonNull View itemView) {
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
			final String position = NotificationRecipientsAdapter.this.getNotificationRecipientsViewModel().getPhoneNumbers().get(this.getAdapterPosition());
			PopupMenu popupMenus = new PopupMenu(NotificationRecipientsAdapter.this.getContext(), v);
			popupMenus.inflate(R.menu.show_menu);
			popupMenus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {

					if (item.getItemId() == R.id.editText) {
						View view = LayoutInflater.from(NotificationRecipientsAdapter.this.getContext()).inflate(R.layout.add_item, null);
						EditText number = view.findViewById(R.id.userNo);
						new AlertDialog.Builder(NotificationRecipientsAdapter.this.getContext()).setView(view).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								NotificationRecipientsAdapter.this.getNotificationRecipientsViewModel().getPhoneNumbers().set(NotificationRecipientsViewHolder.this.getAdapterPosition(), number.getText().toString());
								NotificationRecipientsAdapter.this.notifyDataSetChanged();
								NotificationRecipientsAdapter.this.getNotificationRecipientsViewModel().saveSettings();
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
						new AlertDialog.Builder(NotificationRecipientsAdapter.this.getContext()).setTitle(R.string.delete_confirmation_title).setIcon(R.drawable.ic_warning).setMessage(R.string.delete_confirmation_message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								NotificationRecipientsAdapter.this.getNotificationRecipientsViewModel().getPhoneNumbers().remove(NotificationRecipientsViewHolder.this.getAdapterPosition());
								NotificationRecipientsAdapter.this.notifyDataSetChanged();
								NotificationRecipientsAdapter.this.getNotificationRecipientsViewModel().saveSettings();
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