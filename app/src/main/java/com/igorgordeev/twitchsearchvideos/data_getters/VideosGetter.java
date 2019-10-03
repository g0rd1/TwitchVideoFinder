package com.igorgordeev.twitchsearchvideos.data_getters;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.igorgordeev.twitchsearchvideos.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideosGetter extends AsyncTask<Bundle, Void, List<Video>>
		implements RawDataDownloader.OnDownloadComplete {

	public static final String PERIOD = "period";
	public static final String TYPE = "type";
	public static final String SORT_ORDER = "sort";
	public static final String LANGUAGE = "language";
	public static final String GAME_ID = "game_id";
	public static final String GAME_NAME = "game_name";
	private static final String DATA = "data";
	private static final String USER_NAME = "user_name";
	private static final String TITLE = "title";
	private static final String VIEW_COUNT = "view_count";
	private static final String DURATION = "duration";
	private static final String CREATED_AT = "created_at";
	private static final String THUMBNAIL_URL = "thumbnail_url";
	private static final String ID = "id";
	private static final String URL = "url";
	private static final String FIRST = "first";
	private static final String EMPTY = "";
	private static final int MAX_FIRST = 100;
	private static final String BASE_URL = "https://api.twitch.tv/helix/videos";
	private static final String TAG = "VideosGetter";
	private final OnVideosDataAvailable listener;
	private List<Video> videos = new ArrayList<>();
	private RawDataDownloader.DownloadStatus downloadStatus;
	private String gameName;

	public VideosGetter(OnVideosDataAvailable listener) {
		this.listener = listener;
	}

	@Override
	protected List<Video> doInBackground(Bundle... arguments) {
		RawDataDownloader dataDownloader = new RawDataDownloader(this);
		dataDownloader.runInSameThread(buildUri(arguments[0]), arguments[0].getString(RawDataDownloader.CLIENT_ID));
		return null;
	}

	@Override
	protected void onPostExecute(List<Video> videos) {
		if (listener != null) {
			listener.onVideosDataAvailable(this.videos, downloadStatus);
		}
	}

	private String buildUri(Bundle arguments) {
		String period = arguments.getString(PERIOD, EMPTY);
		String type = arguments.getString(TYPE, EMPTY);
		String sortOrder = arguments.getString(SORT_ORDER, EMPTY);
		String language = arguments.getString(LANGUAGE, EMPTY);
		long gameId = arguments.getLong(GAME_ID);
		gameName = arguments.getString(GAME_NAME);
		return Uri.parse(BASE_URL)
				  .buildUpon()
				  .appendQueryParameter(GAME_ID, String.valueOf(gameId))
				  .appendQueryParameter(PERIOD, period)
				  .appendQueryParameter(TYPE, type)
				  .appendQueryParameter(SORT_ORDER, sortOrder)
				  .appendQueryParameter(LANGUAGE, language)
				  .appendQueryParameter(FIRST, String.valueOf(MAX_FIRST))
				  .build()
				  .toString();
	}

	@Override
	public void onDownloadComplete(String data, RawDataDownloader.DownloadStatus status) {
		downloadStatus = status;
		if (status == RawDataDownloader.DownloadStatus.OK) {
			try {
				JSONArray videosJSONArray = new JSONObject(data).getJSONArray(DATA);
				for (int i = 0; i < videosJSONArray.length(); i++) {
					JSONObject videoJSONObject = videosJSONArray.getJSONObject(i);
					String channelName = videoJSONObject.getString(USER_NAME);
					String title = videoJSONObject.getString(TITLE);
					long views = videoJSONObject.getLong(VIEW_COUNT);
					long id = videoJSONObject.getLong(ID);
					String duration = videoJSONObject.getString(DURATION);
					String type = videoJSONObject.getString(TYPE);
					String createdAt = videoJSONObject.getString(CREATED_AT);
					String thumbnailUrl = videoJSONObject.getString(THUMBNAIL_URL);
					String url = videoJSONObject.getString(URL);
					Video video = new Video(id, Video.NO_GROUP, channelName,
											gameName, title, views, duration, type,
											createdAt, thumbnailUrl, url);
					videos.add(video);
				}
			} catch (JSONException e) {
				Log.e(TAG, "OnDownloadComplete: Invalid data downloaded", e);
			}
		}
	}

	public interface OnVideosDataAvailable {
		void onVideosDataAvailable(List<Video> data, RawDataDownloader.DownloadStatus status);
	}
}
