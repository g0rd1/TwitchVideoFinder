package com.igorgordeev.twitchsearchvideos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.igorgordeev.twitchsearchvideos.contracts.GroupsContract;
import com.igorgordeev.twitchsearchvideos.contracts.GroupsVideoCountContract;
import com.igorgordeev.twitchsearchvideos.contracts.VideosContract;

public class AppDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "TwitchVideoFinder.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "AppDatabase";
	private static AppDatabase instance = null;


	private AppDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	static AppDatabase getInstance(Context context) {
		if (instance == null) {
			Log.d(TAG, "getInstance: creating new instance");
			instance = new AppDatabase(context);
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate: called");
		createGroupsTable(db);
		createVideosTable(db);
		createTriggerDeleteGroup(db);
		createVideosCountView(db);
		insertRecordInGroupsTable(db, "Избранное", false);
		insertRecordInGroupsTable(db, "Посмотреть позже", false);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	private void createGroupsTable(SQLiteDatabase db) {
		String sSQL;
		sSQL = "CREATE TABLE " + GroupsContract.TABLE_NAME + " ("
			   + GroupsContract.Columns._ID + " INTEGER PRIMARY KEY NOT NULL, "
			   + GroupsContract.Columns.NAME + " TEXT NOT NULL, "
			   + GroupsContract.Columns.DELETABLE + " BOOLEAN NOT NULL);";
		Log.d(TAG, sSQL);
		db.execSQL(sSQL);
	}

	private void createVideosTable(SQLiteDatabase db) {
		String sSQL;
		sSQL = "CREATE TABLE " + VideosContract.TABLE_NAME + " ("
			   + VideosContract.Columns._ID + " INTEGER NOT NULL, "
			   + VideosContract.Columns.GROUP_ID + " INTEGER NOT NULL, "
			   + VideosContract.Columns.CHANNEL_NAME + " TEXT NOT NULL, "
			   + VideosContract.Columns.GAME_NAME + " TEXT NOT NULL, "
			   + VideosContract.Columns.TITLE + " TEXT NOT NULL, "
			   + VideosContract.Columns.VIEWS + " INTEGER NOT NULL, "
			   + VideosContract.Columns.DURATION + " INTEGER NOT NULL, "
			   + VideosContract.Columns.TYPE + " TEXT NOT NULL, "
			   + VideosContract.Columns.CREATED_AT + " TEXT NOT NULL, "
			   + VideosContract.Columns.THUMBNAIL_URL + " TEXT NOT NULL, "
			   + VideosContract.Columns.URL + " TEXT NOT NULL, "
			   + "PRIMARY KEY (" + VideosContract.Columns._ID + ", "
			   + VideosContract.Columns.GROUP_ID + "));";
		Log.d(TAG, sSQL);
		db.execSQL(sSQL);
	}

	private void createTriggerDeleteGroup(SQLiteDatabase db) {
		String sSQL;
		sSQL = "CREATE TRIGGER Remove_Group"
			   + " AFTER DELETE ON " + GroupsContract.TABLE_NAME
			   + " FOR EACH ROW"
			   + " BEGIN"
			   + " DELETE FROM " + VideosContract.TABLE_NAME
			   + " WHERE " + VideosContract.Columns.GROUP_ID + " = OLD."
			   + GroupsContract.Columns._ID
			   + ";"
			   + " END;";
		Log.d(TAG, sSQL);
		db.execSQL(sSQL);
	}

	private void createVideosCountView(SQLiteDatabase db) {
		/*CREATE VIEW vwVideosCount AS SELECT Groups._id, Groups.Name, Groups.Deletable,
		COUNT(Videos._id) AS VideoCount,
		FROM Groups LEFT JOIN Videos
		ON Groups._id = Videos.GroupId GROUP BY Groups._id;*/
		String sSQL;
		sSQL = "CREATE VIEW " + GroupsVideoCountContract.TABLE_NAME + " AS SELECT "
			   + GroupsContract.TABLE_NAME + "." + GroupsContract.Columns._ID + ", "
			   + GroupsContract.TABLE_NAME + "." + GroupsContract.Columns.NAME + ", "
			   + GroupsContract.TABLE_NAME + "." + GroupsContract.Columns.DELETABLE + ", "
			   + "COUNT(" + VideosContract.TABLE_NAME + "." + VideosContract.Columns._ID + ") AS "
			   + GroupsVideoCountContract.Columns.VIDEO_COUNT + " FROM "
			   + GroupsContract.TABLE_NAME + " LEFT JOIN " + VideosContract.TABLE_NAME + " ON "
			   + GroupsContract.TABLE_NAME + "." + GroupsContract.Columns._ID + " = "
			   + VideosContract.TABLE_NAME + "." + VideosContract.Columns.GROUP_ID
			   + " GROUP BY " + GroupsContract.TABLE_NAME + "." + GroupsContract.Columns._ID + ";";
		Log.d(TAG, sSQL);
		db.execSQL(sSQL);
	}

	private void insertRecordInGroupsTable(SQLiteDatabase db, String name, boolean deletable) {
		String sSQL;
		int deletableInt = deletable ? 1 : 0;
		sSQL = "INSERT INTO " + GroupsContract.TABLE_NAME + "("
			   + GroupsContract.Columns.NAME + ", " + GroupsContract.Columns.DELETABLE
			   + ") VALUES ('" + name + "', " + deletableInt + ");";
		Log.d(TAG, sSQL);
		db.execSQL(sSQL);
	}
}
