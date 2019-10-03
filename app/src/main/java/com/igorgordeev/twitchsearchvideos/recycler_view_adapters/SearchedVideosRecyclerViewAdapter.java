package com.igorgordeev.twitchsearchvideos.recycler_view_adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.igorgordeev.twitchsearchvideos.R;
import com.igorgordeev.twitchsearchvideos.model.Video;

import java.util.List;

public class SearchedVideosRecyclerViewAdapter extends AbstractVideosRecyclerViewAdapter {

	private List<Video> videos;
	private OnSearchedVideosClickListener listener;

	/**
	 * @param videos   videos list to display in recycler view adapter
	 * @param listener listener must implement {@link androidx.fragment.app.Fragment}
	 *                 to get access to app resources
	 */
	public SearchedVideosRecyclerViewAdapter(@NonNull List<Video> videos,
											 @NonNull OnSearchedVideosClickListener listener) {
		if (!(listener instanceof Fragment)) {
			throw new IllegalArgumentException("Listener must implement Fragment interface");
		}
		this.videos = videos;
		this.listener = listener;
	}

	@Override
	public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
		final Video video = videos.get(position);
		putVideoDataInView(holder, video, ((Fragment) listener).getResources());
		holder.addOrDelete.setImageResource(R.drawable.ic_menu_add);
		holder.addOrDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onSaveVideoClick(video);
			}
		});
		holder.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onVideoClick(video);
			}
		});
	}

	@Override
	public int getItemCount() {
		return videos == null ? 0 : videos.size();
	}

	public List<Video> swapVideos(List<Video> newVideos) {
		if (newVideos == videos) {
			return null;
		}

		int numItems = getItemCount();

		final List<Video> oldVideos = videos;
		videos = newVideos;
		if (newVideos != null) {
			// notify the observers dialog_about the new videos
			notifyDataSetChanged();
		} else {
			// notify the observers dialog_about the lack of a data set
			notifyItemRangeRemoved(0, numItems);
		}
		return oldVideos;
	}

	public interface OnSearchedVideosClickListener {
		void onSaveVideoClick(Video video);

		void onVideoClick(Video video);
	}
}
