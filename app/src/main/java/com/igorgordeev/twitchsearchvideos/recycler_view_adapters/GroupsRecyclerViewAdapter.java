package com.igorgordeev.twitchsearchvideos.recycler_view_adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igorgordeev.twitchsearchvideos.R;
import com.igorgordeev.twitchsearchvideos.contracts.GroupsVideoCountContract;
import com.igorgordeev.twitchsearchvideos.model.Group;

import java.util.Locale;

public class GroupsRecyclerViewAdapter extends RecyclerView.Adapter<GroupsRecyclerViewAdapter.GroupViewHolder> {

	private Cursor cursor;
	private OnGroupClickListener listener;

	public GroupsRecyclerViewAdapter(Cursor cursor, OnGroupClickListener listener) {
		this.cursor = cursor;
		this.listener = listener;
	}

	@NonNull
	@Override
	public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
								  .inflate(R.layout.item_groups_list, parent, false);
		return new GroupViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
		if (cursor == null || cursor.getCount() == 0 /* || cursor.isClosed() */) {
			return;
		}
		if (!cursor.moveToPosition(position)) {
			throw new IllegalArgumentException("Couldn't move cursor to position " + position);
		}
		long id = cursor.getLong(cursor.getColumnIndex(GroupsVideoCountContract.Columns._ID));
		String name = cursor.getString(cursor.getColumnIndex(GroupsVideoCountContract.Columns.NAME));
		boolean deletable = cursor.getInt(cursor.getColumnIndex(GroupsVideoCountContract.Columns.DELETABLE)) != 0;
		final Group group = new Group(id, name, deletable);
		final long count = cursor.getLong(cursor.getColumnIndex(GroupsVideoCountContract.Columns.VIDEO_COUNT));
		holder.name.setText(group.getName());
		holder.count.setText(String.format(Locale.getDefault(), "%d видео", count));
		if (group.isDeletable()) {
			holder.delete.setVisibility(View.VISIBLE);
			holder.edit.setVisibility(View.VISIBLE);
			holder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.OnDeleteGroupClick(group, count);
				}
			});
			holder.edit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.OnEditGroupClick(group);
				}
			});
		} else {
			holder.delete.setVisibility(View.INVISIBLE);
			holder.edit.setVisibility(View.INVISIBLE);
		}
		holder.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.OnGroupClick(group, count);
			}
		});
	}

	@Override
	public int getItemCount() {
		return cursor != null ? cursor.getCount() : 0;
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == cursor) {
			return null;
		}

		int numItems = getItemCount();

		final Cursor oldCursor = cursor;
		cursor = newCursor;
		if (newCursor != null) {
			// notify the observers dialog_about the new cursor
			notifyDataSetChanged();
		} else {
			// notify the observers dialog_about the lack of a data set
			notifyItemRangeRemoved(0, numItems);
		}
		return oldCursor;
	}

	public interface OnGroupClickListener {
		void OnEditGroupClick(Group group);

		void OnDeleteGroupClick(Group group, long count);

		void OnGroupClick(Group group, long count);
	}

	static class GroupViewHolder extends RecyclerView.ViewHolder {

		ImageView edit;
		ImageView delete;
		TextView name;
		TextView count;
		View view;

		GroupViewHolder(@NonNull View itemView) {
			super(itemView);
			edit = itemView.findViewById(R.id.groups_list_item_edit);
			delete = itemView.findViewById(R.id.groups_list_item_delete);
			name = itemView.findViewById(R.id.groups_list_item_group_name);
			count = itemView.findViewById(R.id.groups_list_item_video_count);
			view = itemView;
		}
	}
}
