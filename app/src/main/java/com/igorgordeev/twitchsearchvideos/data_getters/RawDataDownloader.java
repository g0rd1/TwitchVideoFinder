package com.igorgordeev.twitchsearchvideos.data_getters;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class RawDataDownloader extends AsyncTask<String, Void, String> {

	public static final String CLIENT_ID = "Client-ID";
	private static final String TAG = "GetRawData";
	private final OnDownloadComplete listener;
	private DownloadStatus downloadStatus;

	public RawDataDownloader(OnDownloadComplete listener) {
		this.downloadStatus = DownloadStatus.IDLE;
		this.listener = listener;
	}

	public void runInSameThread(String... strings) {
		Log.d(TAG, "runInSameThread: starts");
		if (listener != null) {
			listener.onDownloadComplete(doInBackground(strings), downloadStatus);
		}
		Log.d(TAG, "runInSameThread: ends");
	}

	/**
	 * @param strings strings[0] must contain url
	 *                strings[1] must contain API token
	 */
	@Override
	protected String doInBackground(String... strings) {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		if (strings == null) {
			downloadStatus = DownloadStatus.NOT_INITIALISED;
			return null;
		}

		try {
			downloadStatus = DownloadStatus.PROCESSING;
			Log.d(TAG, "doInBackground: downloading from url " + strings[0]);
			URL url = new URL(strings[0]);
			String clientId = strings[1];
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(CLIENT_ID, clientId);
			connection.setRequestMethod("GET");

			StringBuilder result = new StringBuilder();

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;
			while (null != (line = reader.readLine())) {
				result.append(line).append("\n");
			}
			downloadStatus = DownloadStatus.OK;
			return result.toString();
		} catch (MalformedURLException e) {
			Log.e(TAG, "doInBackground: Invalid Url", e);
		} catch (IOException e) {
			Log.e(TAG, "doInBackground: IO Exception reading data", e);
		} catch (SecurityException e) {
			Log.e(TAG, "doInBackground: Security Exception. Needs permission?", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e(TAG, "doInBackground: Error closing stream", e);
				}
			}
		}

		downloadStatus = DownloadStatus.FAILED_OR_EMPTY;

		return null;
	}

	@Override
	protected void onPostExecute(String s) {
		if (listener != null) {
			listener.onDownloadComplete(s, downloadStatus);
		}
		Log.d(TAG, "onPostExecute: ends");
	}

	public enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}

	public interface OnDownloadComplete {
		void onDownloadComplete(String data, DownloadStatus status);
	}

}
