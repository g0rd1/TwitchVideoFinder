package com.igorgordeev.twitchsearchvideos;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igorgordeev.twitchsearchvideos.contracts.GroupsContract;
import com.igorgordeev.twitchsearchvideos.contracts.GroupsVideoCountContract;
import com.igorgordeev.twitchsearchvideos.contracts.VideosContract;

public class AppProvider extends ContentProvider {

	public static final String CONTENT_AUTHORITY = "com.igorgordeev.twitchsearchvideos.provider";
	public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	private static final String TAG = "AppProvider";
	private static final int GROUPS = 100;
	private static final int GROUPS_ID = 101;
	private static final int VIDEOS = 200;
	private static final int VIDEOS_ID = 201;
	private static final int GROUPS_VIDEO_COUNT = 300;
	private static final int GROUPS_VIDEO_COUNT_ID = 301;
	private static final UriMatcher uriMatcher = buildUriMatcher();
	private AppDatabase openHelper;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(CONTENT_AUTHORITY, GroupsContract.TABLE_NAME, GROUPS);
		matcher.addURI(CONTENT_AUTHORITY, GroupsContract.TABLE_NAME + "/#", GROUPS_ID);

		matcher.addURI(CONTENT_AUTHORITY, VideosContract.TABLE_NAME, VIDEOS);
		matcher.addURI(CONTENT_AUTHORITY, VideosContract.TABLE_NAME + "/#", VIDEOS_ID);

		matcher.addURI(CONTENT_AUTHORITY, GroupsVideoCountContract.TABLE_NAME, GROUPS_VIDEO_COUNT);
		matcher.addURI(CONTENT_AUTHORITY,
					   GroupsVideoCountContract.TABLE_NAME + "/#",
					   GROUPS_VIDEO_COUNT_ID);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		openHelper = AppDatabase.getInstance(getContext());
		return true;
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri,
						@Nullable String[] projection,
						@Nullable String selection,
						@Nullable String[] selectionArgs,
						@Nullable String sortOrder) {
		Log.d(TAG, "query: called with URI: " + uri);
		final int match = uriMatcher.match(uri);
		Log.d(TAG, "query: match is: " + match);
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		switch (match) {
			case GROUPS:
				queryBuilder.setTables(GroupsContract.TABLE_NAME);
				break;
			case GROUPS_ID:
				queryBuilder.setTables(GroupsContract.TABLE_NAME);
				long groupId = GroupsContract.getGroupId(uri);
				queryBuilder.appendWhere(GroupsContract.Columns._ID + " = " + groupId);
				break;

			case VIDEOS:
				queryBuilder.setTables(VideosContract.TABLE_NAME);
				break;
			case VIDEOS_ID:
				queryBuilder.setTables(VideosContract.TABLE_NAME);
				long videoId = VideosContract.getVideoId(uri);
				queryBuilder.appendWhere(VideosContract.Columns._ID + " = " + videoId);
				break;

			case GROUPS_VIDEO_COUNT:
				queryBuilder.setTables(GroupsVideoCountContract.TABLE_NAME);
				break;
			case GROUPS_VIDEO_COUNT_ID:
				queryBuilder.setTables(GroupsVideoCountContract.TABLE_NAME);
				long countId = GroupsVideoCountContract.getCountId(uri);
				queryBuilder.appendWhere(GroupsVideoCountContract.Columns._ID + " = " + countId);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db,
										   projection,
										   selection,
										   selectionArgs,
										   null,
										   null,
										   sortOrder);
		Log.d(TAG, "query: rows in returned cursor = " + cursor.getCount());
		Context context = getContext();
		if (context != null) {
			cursor.setNotificationUri(context.getContentResolver(), uri);
		}
		return cursor;
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		final int match = uriMatcher.match(uri);
		switch (match) {
			case GROUPS:
				return GroupsContract.CONTENT_TYPE;
			case GROUPS_ID:
				return GroupsContract.CONTENT_ITEM_TYPE;

			case VIDEOS:
				return VideosContract.CONTENT_TYPE;
			case VIDEOS_ID:
				return VideosContract.CONTENT_ITEM_TYPE;

			case GROUPS_VIDEO_COUNT:
				return GroupsVideoCountContract.CONTENT_TYPE;
			case GROUPS_VIDEO_COUNT_ID:
				return GroupsVideoCountContract.CONTENT_ITEM_TYPE;

			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
		Log.d(TAG, "Entering insert, called with uri: " + uri);
		final int match = uriMatcher.match(uri);
		Log.d(TAG, "match is " + match);

		final SQLiteDatabase db;

		Uri returnUri;
		long recordId;
		switch (match) {
			case GROUPS:
				db = openHelper.getWritableDatabase();
				recordId = db.insert(GroupsContract.TABLE_NAME, null, values);
				if (recordId >= 0) {
					returnUri = GroupsContract.buildGroupUri(recordId);
				} else {
					throw new SQLException("Failed to insert into " + uri.toString());
				}
				break;
			case VIDEOS:
				db = openHelper.getWritableDatabase();
				recordId = db.insert(VideosContract.TABLE_NAME, null, values);
				if (recordId >= 0) {
					returnUri = VideosContract.buildVideoUri(recordId);
				} else {
					throw new SQLException("Failed to insert into " + uri.toString());
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		if (recordId > 0) {
			Context context = getContext();
			if (context != null) {
				ContentResolver contentResolver = context.getContentResolver();
				contentResolver.notifyChange(uri, null);
				if (match == VIDEOS || match == GROUPS) {
					contentResolver.notifyChange(GroupsVideoCountContract.CONTENT_URI, null);
				}
				Log.d(TAG, "Exiting insert, returning " + recordId);
			}
		} else {
			Log.d(TAG, "insert: nothing inserted");
		}

		return returnUri;
	}

	@Override
	public int delete(@NonNull Uri uri,
					  @Nullable String selection,
					  @Nullable String[] selectionArgs) {
		Log.d(TAG, "delete called with uri: " + uri);
		final int match = uriMatcher.match(uri);
		Log.d(TAG, "match is " + match);

		final SQLiteDatabase db;
		int count;

		String selectionCriteria;

		switch (match) {
			case GROUPS:
				db = openHelper.getWritableDatabase();
				count = db.delete(GroupsContract.TABLE_NAME, selection, selectionArgs);
				break;
			case GROUPS_ID:
				db = openHelper.getWritableDatabase();
				long groupId = GroupsContract.getGroupId(uri);
				selectionCriteria = GroupsContract.Columns._ID + " = " + groupId;
				if (selection != null && !selection.isEmpty()) {
					selectionCriteria += " AND (" + selection + ")";
				}
				count = db.delete(GroupsContract.TABLE_NAME, selectionCriteria, selectionArgs);
				break;

			case VIDEOS:
				db = openHelper.getWritableDatabase();
				count = db.delete(VideosContract.TABLE_NAME, selection, selectionArgs);
				break;
			case VIDEOS_ID:
				db = openHelper.getWritableDatabase();
				long videoId = VideosContract.getVideoId(uri);
				selectionCriteria = VideosContract.Columns._ID + " = " + videoId;
				if (selection != null && !selection.isEmpty()) {
					selectionCriteria += " AND (" + selection + ")";
				}
				count = db.delete(VideosContract.TABLE_NAME, selectionCriteria, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		if (count > 0) {
			Context context = getContext();
			if (context != null) {
				ContentResolver contentResolver = context.getContentResolver();
				contentResolver.notifyChange(uri, null);
				if (match == VIDEOS || match == GROUPS || match == VIDEOS_ID || match == GROUPS_ID) {
					contentResolver.notifyChange(GroupsVideoCountContract.CONTENT_URI, null);
				}
			}
		} else {
			Log.d(TAG, "delete: nothing deleted");
		}
		Log.d(TAG, "Exiting delete, returning " + count);
		return count;
	}

	@Override
	public int update(@NonNull Uri uri,
					  @Nullable ContentValues values,
					  @Nullable String selection,
					  @Nullable String[] selectionArgs) {
		Log.d(TAG, "update called with uri: " + uri);
		final int match = uriMatcher.match(uri);
		Log.d(TAG, "match is " + match);

		final SQLiteDatabase db;
		int count;

		String selectionCriteria;

		switch (match) {
			case GROUPS:
				db = openHelper.getWritableDatabase();
				count = db.update(GroupsContract.TABLE_NAME, values, selection, selectionArgs);
				break;
			case GROUPS_ID:
				db = openHelper.getWritableDatabase();
				long groupId = GroupsContract.getGroupId(uri);
				selectionCriteria = GroupsContract.Columns._ID + " = " + groupId;
				if (selection != null && !selection.isEmpty()) {
					selectionCriteria += " AND (" + selection + ")";
				}
				count = db.update(GroupsContract.TABLE_NAME,
								  values,
								  selectionCriteria,
								  selectionArgs);
				break;

			case VIDEOS:
				db = openHelper.getWritableDatabase();
				count = db.update(VideosContract.TABLE_NAME, values, selection, selectionArgs);
				break;
			case VIDEOS_ID:
				db = openHelper.getWritableDatabase();
				long videoId = VideosContract.getVideoId(uri);
				selectionCriteria = VideosContract.Columns._ID + " = " + videoId;
				if (selection != null && !selection.isEmpty()) {
					selectionCriteria += " AND (" + selection + ")";
				}
				count = db.update(VideosContract.TABLE_NAME,
								  values,
								  selectionCriteria,
								  selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		if (count > 0) {
			Log.d(TAG, "update: Setting notifyChanged with " + uri);
			Context context = getContext();
			if (context != null) {
				ContentResolver contentResolver = context.getContentResolver();
				contentResolver.notifyChange(uri, null);
				if (match == VIDEOS || match == GROUPS || match == VIDEOS_ID || match == GROUPS_ID) {
					contentResolver.notifyChange(GroupsVideoCountContract.CONTENT_URI, null);
				}
			}
		} else {
			Log.d(TAG, "update: nothing updated");
		}
		Log.d(TAG, "Exiting update, returning " + count);
		return count;
	}
}
