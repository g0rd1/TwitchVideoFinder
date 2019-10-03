package com.igorgordeev.twitchsearchvideos.contracts;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.igorgordeev.twitchsearchvideos.AppProvider;

public class GroupsVideoCountContract {

	public static final String TABLE_NAME = "vwGroupsVideoCount";

	public static final Uri CONTENT_URI = Uri.withAppendedPath(AppProvider.CONTENT_AUTHORITY_URI,
															   TABLE_NAME);
	public static final String CONTENT_TYPE =
			"vnd.android.cursor.dir/vnd." + AppProvider.CONTENT_AUTHORITY + "." + TABLE_NAME;

	public static final String CONTENT_ITEM_TYPE =
			"vnd.android.cursor.item/vnd." + AppProvider.CONTENT_AUTHORITY + "." + TABLE_NAME;

	public static Uri buildCountUri(long countId) {
		return ContentUris.withAppendedId(CONTENT_URI, countId);
	}

	public static long getCountId(Uri uri) {
		return ContentUris.parseId(uri);
	}

	public static abstract class Columns {
		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "Name";
		public static final String DELETABLE = "Deletable";
		public static final String VIDEO_COUNT = "VideoCount";
	}
}