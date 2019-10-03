package com.igorgordeev.twitchsearchvideos.recycler_view_adapters;

import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igorgordeev.twitchsearchvideos.R;
import com.igorgordeev.twitchsearchvideos.model.Video;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class AbstractVideosRecyclerViewAdapter extends RecyclerView.Adapter<AbstractVideosRecyclerViewAdapter.VideoViewHolder> {

	private static final String THUMBNAIL_WIDTH_VALUE = "320";
	private static final String THUMBNAIL_HEIGHT_VALUE = "180";
	private static final String THUMBNAIL_WIDTH_PARAMETER = "%{width}";
	private static final String THUMBNAIL_HEIGHT_PARAMETER = "%{height}";
	private static final String TAG = "AbstractVideosRVA";
	private static final String HTTPS = "https";
	private static final String HTTP = "http";

	@NonNull
	@Override
	public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
								  .inflate(R.layout.item_videos_list, parent, false);
		return new VideoViewHolder(view);
	}

	void putVideoDataInView(VideoViewHolder holder, Video video, Resources resources) {
		if (video.getThumbnailUrl().isEmpty()) {
			holder.thumbnail.setImageResource(R.drawable.placeholder);
		} else {
			Log.d(TAG, "putVideoDataInView: thumbnailUrl - " + formatThumbnailUrl(video.getThumbnailUrl()));
			Picasso.get()
				   .load(formatThumbnailUrl(video.getThumbnailUrl()))
				   .error(R.drawable.placeholder)
				   .placeholder(R.drawable.placeholder)
				   .into(holder.thumbnail);
		}
		holder.channelName.setText(video.getChannelName());
		holder.gameName.setText(video.getGameName());
		holder.duration.setText(formatDuration(video.getDuration()));
		holder.views.setText(String.format(Locale.getDefault(), "%d %s", video.getViews(),
										   resources.getString(R.string.views_reduction)));
		holder.title.setText(video.getTitle());
		String[] types = resources.getStringArray(R.array.type);
		String[] typesParameters = resources.getStringArray(R.array.type_parameter);
		String type = "";
		for (int i = 0; i < typesParameters.length; i++) {
			if (video.getType().equals(typesParameters[i])) {
				type = types[i];
			}
		}
		holder.type.setText(type);
		Date createdAt = new Date();
		try {
			createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(video.getCreatedAt());
		} catch (ParseException e) {
			Log.e(TAG, "onBindViewHolder: Invalid date format", e);
		}
		holder.createdAt.setText(DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(createdAt));
	}

	private String formatThumbnailUrl(String thumbnailUrl) {
		String formattedUrl = thumbnailUrl.replace(THUMBNAIL_WIDTH_PARAMETER, THUMBNAIL_WIDTH_VALUE)
										  .replace(THUMBNAIL_HEIGHT_PARAMETER, THUMBNAIL_HEIGHT_VALUE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return formattedUrl.replace(HTTPS, HTTP);
		} else {
			return formattedUrl;
		}
	}

	private String formatDuration(String duration) {
		String[] split = duration.split("\\D");
		long seconds = 0;
		long minutes = 0;
		long hours = 0;
		switch (split.length) {
			case 1:
				seconds = Long.parseLong(split[0]);
				break;
			case 2:
				seconds = Long.parseLong(split[1]);
				minutes = Long.parseLong(split[0]);
				break;
			case 3:
				seconds = Long.parseLong(split[2]);
				minutes = Long.parseLong(split[1]);
				hours = Long.parseLong(split[0]);
				break;
		}
		return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
	}

	static class VideoViewHolder extends RecyclerView.ViewHolder {

		ImageView thumbnail;
		ImageView addOrDelete;
		TextView channelName;
		TextView gameName;
		TextView title;
		TextView createdAt;
		TextView type;
		TextView views;
		TextView duration;
		View view;

		VideoViewHolder(@NonNull View itemView) {
			super(itemView);
			thumbnail = itemView.findViewById(R.id.videos_list_item_thumbnail);
			addOrDelete = itemView.findViewById(R.id.videos_list_add_or_delete);
			channelName = itemView.findViewById(R.id.videos_list_item_channel_name);
			gameName = itemView.findViewById(R.id.videos_list_item_game_name);
			title = itemView.findViewById(R.id.videos_list_item_title);
			createdAt = itemView.findViewById(R.id.videos_list_item_created_at);
			type = itemView.findViewById(R.id.videos_list_item_type);
			views = itemView.findViewById(R.id.videos_list_item_views);
			duration = itemView.findViewById(R.id.videos_list_item_duration);
			view = itemView;
		}

	}

}
