package com.igorgordeev.twitchsearchvideos.recycler_view_adapters;

import android.database.Cursor;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.igorgordeev.twitchsearchvideos.R;
import com.igorgordeev.twitchsearchvideos.contracts.VideosContract;
import com.igorgordeev.twitchsearchvideos.model.Video;

public class SavedVideosRecyclerViewAdapter extends AbstractVideosRecyclerViewAdapter {

	private Cursor cursor;
	private OnSavedVideosClickListener listener;

	/**
	 * @param cursor   cursor with videos to display in recycler view adapter
	 * @param listener listener must implement {@link androidx.fragment.app.Fragment}
	 *                 to get access to app resources
	 */
	public SavedVideosRecyclerViewAdapter(Cursor cursor, @NonNull OnSavedVideosClickListener listener) {
		this.cursor = cursor;
		this.listener = listener;
		if (!(listener instanceof Fragment)) {
			throw new IllegalArgumentException("Listener must implement Fragment interface");
		}
	}

	@Override
	public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
		if (cursor == null || cursor.getCount() == 0 || cursor.isClosed()) {
			return;
		}
		if (!cursor.moveToPosition(position)) {
			throw new IllegalArgumentException("Couldn't move cursor to position " + position);
		}
		final Video video = new Video(cursor.getLong(cursor.getColumnIndex(VideosContract.Columns._ID)),
									  cursor.getLong(cursor.getColumnIndex(VideosContract.Columns.GROUP_ID)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.CHANNEL_NAME)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.GAME_NAME)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.TITLE)),
									  cursor.getLong(cursor.getColumnIndex(VideosContract.Columns.VIEWS)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.DURATION)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.TYPE)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.CREATED_AT)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.THUMBNAIL_URL)),
									  cursor.getString(cursor.getColumnIndex(VideosContract.Columns.URL)));
		putVideoDataInView(holder, video, ((Fragment) listener).getResources());
		holder.addOrDelete.setImageResource(R.drawable.ic_menu_delete);
		holder.addOrDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onDeleteSavedVideoClick(video);
			}
		});
		holder.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onSavedVideoClick(video);
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

	public interface OnSavedVideosClickListener {
		void onDeleteSavedVideoClick(Video video);

		void onSavedVideoClick(Video video);
	}

}
