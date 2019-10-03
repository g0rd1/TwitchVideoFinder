package com.igorgordeev.twitchsearchvideos.contracts;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.igorgordeev.twitchsearchvideos.AppProvider;

public class VideosContract {

	public static final String TABLE_NAME = "Videos";

	public static final Uri CONTENT_URI = Uri.withAppendedPath(AppProvider.CONTENT_AUTHORITY_URI,
															   TABLE_NAME);
	public static final String CONTENT_TYPE =
			"vnd.android.cursor.dir/vnd." + AppProvider.CONTENT_AUTHORITY + "." + TABLE_NAME;

	public static final String CONTENT_ITEM_TYPE =
			"vnd.android.cursor.item/vnd." + AppProvider.CONTENT_AUTHORITY + "." + TABLE_NAME;

	public static Uri buildVideoUri(long videoId) {
		return ContentUris.withAppendedId(CONTENT_URI, videoId);
	}

	public static long getVideoId(Uri uri) {
		return ContentUris.parseId(uri);
	}

	public static abstract class Columns {
		public static final String _ID = BaseColumns._ID;
		public static final String GROUP_ID = "GroupId";
		public static final String CHANNEL_NAME = "ChannelName";
		public static final String GAME_NAME = "Game";
		public static final String TITLE = "Title";
		public static final String VIEWS = "Views";
		public static final String DURATION = "Duration";
		public static final String TYPE = "Type";
		public static final String CREATED_AT = "CreatedAt";
		public static final String THUMBNAIL_URL = "Thumbnail";
		public static final String URL = "Url";
	}
}
