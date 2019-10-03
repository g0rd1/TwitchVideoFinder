package com.igorgordeev.twitchsearchvideos.data_getters;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.igorgordeev.twitchsearchvideos.model.Game;

import org.json.JSONException;
import org.json.JSONObject;

public class GameGetter extends AsyncTask<String, Void, Game> implements RawDataDownloader.OnDownloadComplete {

	private static final String BASE_URL = "https://api.twitch.tv/helix/games";
	private static final String NAME = "name";
	private static final String TAG = "GameGetter";

	private final OnGameIdAvailable listener;
	private Game game = null;
	private RawDataDownloader.DownloadStatus downloadStatus;

	public GameGetter(OnGameIdAvailable listener) {
		this.listener = listener;
	}

	@Override
	protected Game doInBackground(String... strings) {
		RawDataDownloader dataDownloader = new RawDataDownloader(this);
		dataDownloader.runInSameThread(buildUri(strings[0]), strings[1]);
		return null;
	}

	private String buildUri(String gameName) {
		return Uri.parse(BASE_URL).buildUpon().appendQueryParameter(NAME, gameName).toString();
	}

	@Override
	protected void onPostExecute(Game g) {
		if (listener != null) {
			listener.onGameIdAvailable(game, downloadStatus);
		}
	}

	@Override
	public void onDownloadComplete(String data, RawDataDownloader.DownloadStatus status) {
		downloadStatus = status;
		if (status == RawDataDownloader.DownloadStatus.OK) {
			try {
				JSONObject dataObject = new JSONObject(data).getJSONArray("data").getJSONObject(0);
				String gameName = dataObject.getString("name");
				long gameId = dataObject.getLong("id");
				game = new Game(gameName, gameId);
			} catch (JSONException e) {
				Log.e(TAG, "OnDownloadComplete: Invalid data downloaded", e);
			}
		}
	}

	public interface OnGameIdAvailable {
		void onGameIdAvailable(Game gameId, RawDataDownloader.DownloadStatus status);
	}
}
