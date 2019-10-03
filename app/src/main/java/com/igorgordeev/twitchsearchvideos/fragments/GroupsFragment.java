package com.igorgordeev.twitchsearchvideos.fragments;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igorgordeev.twitchsearchvideos.MainActivity;
import com.igorgordeev.twitchsearchvideos.R;
import com.igorgordeev.twitchsearchvideos.contracts.GroupsContract;
import com.igorgordeev.twitchsearchvideos.contracts.GroupsVideoCountContract;
import com.igorgordeev.twitchsearchvideos.contracts.VideosContract;
import com.igorgordeev.twitchsearchvideos.dialogs.AppDialog;
import com.igorgordeev.twitchsearchvideos.model.Group;
import com.igorgordeev.twitchsearchvideos.model.Video;
import com.igorgordeev.twitchsearchvideos.recycler_view_adapters.GroupsRecyclerViewAdapter;
import com.igorgordeev.twitchsearchvideos.recycler_view_adapters.SavedVideosRecyclerViewAdapter;

public class GroupsFragment extends Fragment implements GroupsRecyclerViewAdapter.OnGroupClickListener,
														SavedVideosRecyclerViewAdapter.OnSavedVideosClickListener,
														LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "GroupsFragment";
	private static final String SELECTION_PARAM = "SELECTION";
	private static final String SELECTION_ARGS_PARAM = "SELECTION_ARGS";
	private static final String SORT_ORDER_PARAM = "SORT_ORDER";
	private static final String GROUP = "GROUP";
	private final int GROUPS_VIDEO_COUNT_LOADER_ID = 1;
	private final int VIDEOS_LOADER_ID = 2;
	private final int DELETE_GROUP_DIALOG_ID = 1;
	private final int EDIT_GROUP_NAME_DIALOG_ID = 2;
	private final int ADD_GROUP_DIALOG_ID = 3;
	private GroupsRecyclerViewAdapter groupsAdapter;
	private SavedVideosRecyclerViewAdapter videosAdapter;
	private RecyclerView groupsRecyclerView;
	private RecyclerView videosRecyclerView;
	private boolean isGroupsRecyclerViewActive = true;
	private int orientation;

	public GroupsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		View view = inflater.inflate(R.layout.fragment_groups, container, false);
		setOrientationValue();
		configRecyclerView(view);
		setRecyclerViewVisibility();
		return view;
	}

	private void setOrientationValue() {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			orientation = activity.getResources().getConfiguration().orientation;
		}
	}

	private void configRecyclerView(View view) {
		groupsRecyclerView = view.findViewById(R.id.groups_fragment_groups_list);
		videosRecyclerView = view.findViewById(R.id.groups_fragment_videos_list);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
		linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
		groupsRecyclerView.setLayoutManager(linearLayoutManager);
		videosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		if (groupsAdapter == null) {
			groupsAdapter = new GroupsRecyclerViewAdapter(null, this);
		}
		if (videosAdapter == null) {
			videosAdapter = new SavedVideosRecyclerViewAdapter(null, this);
		}
		groupsRecyclerView.setAdapter(groupsAdapter);
		videosRecyclerView.setAdapter(videosAdapter);
	}

	private void setRecyclerViewVisibility() {
		if (orientation == Configuration.ORIENTATION_PORTRAIT && videosRecyclerView.getVisibility() == View.VISIBLE
			&& groupsRecyclerView.getVisibility() == View.VISIBLE) {
			videosRecyclerView.setVisibility(View.GONE);
		}
		if (!isGroupsRecyclerViewActive && videosRecyclerView.getVisibility() == View.GONE) {
			swapRecyclerViewsVisibility();
		}
		if (orientation == Configuration.ORIENTATION_PORTRAIT && !isGroupsRecyclerViewActive
			&& videosAdapter.getItemCount() == 0) {
			swapRecyclerViewsVisibility();
		}
	}

	public void swapRecyclerViewsVisibility() {
		MainActivity activity = (MainActivity) getActivity();
		if (groupsRecyclerView.getVisibility() == View.VISIBLE) {
			groupsRecyclerView.setVisibility(View.GONE);
			videosRecyclerView.setVisibility(View.VISIBLE);
			isGroupsRecyclerViewActive = false;
			if (activity != null) {
				activity.setUpButtonVisibility(true);
			}
		} else {
			groupsRecyclerView.setVisibility(View.VISIBLE);
			videosRecyclerView.setVisibility(View.GONE);
			isGroupsRecyclerViewActive = true;
			if (activity != null) {
				activity.setUpButtonVisibility(false);
			}
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated: starts");
		super.onActivityCreated(savedInstanceState);
		LoaderManager.getInstance(this).initLoader(GROUPS_VIDEO_COUNT_LOADER_ID, null, this);
		LoaderManager.getInstance(this).initLoader(VIDEOS_LOADER_ID, null, this);
	}

	public void addNewGroup() {
		AppDialog dialog = new AppDialog();
		Bundle arguments = new Bundle();
		arguments.putInt(AppDialog.DIALOG_ID, ADD_GROUP_DIALOG_ID);
		arguments.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.add_group_dialog_negative_choice);
		arguments.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.add_group_dialog_positive_choice);
		arguments.putBoolean(AppDialog.DIALOG_WITH_TEXTBOX, true);
		arguments.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.add_group_dialog_message));
		arguments.putString(AppDialog.DIALOG_TEXTBOX_HINT, getString(R.string.add_group_textbox_hint));
		dialog.setArguments(arguments);
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null) {
			dialog.show(fragmentManager, null);
		}
	}

	@Override
	public void OnEditGroupClick(Group group) {
		AppDialog dialog = new AppDialog();
		Bundle arguments = new Bundle();
		arguments.putInt(AppDialog.DIALOG_ID, EDIT_GROUP_NAME_DIALOG_ID);
		arguments.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.edit_group_name_dialog_negative_choice);
		arguments.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.edit_group_name_dialog_positive_choice);
		arguments.putBoolean(AppDialog.DIALOG_WITH_TEXTBOX, true);
		arguments.putString(AppDialog.DIALOG_MESSAGE,
							getString(R.string.edit_group_name_dialog_message, group.getName()));
		arguments.putString(AppDialog.DIALOG_TEXTBOX_HINT, getString(R.string.edit_group_name_textbox_hint));
		arguments.putSerializable(GROUP, group);
		dialog.setArguments(arguments);
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null) {
			dialog.show(fragmentManager, null);
		}
	}

	@Override
	public void OnDeleteGroupClick(Group group, long count) {
		if (count == 0) {
			deleteGroup(group.getId());
			return;
		}
		AppDialog dialog = new AppDialog();
		Bundle arguments = new Bundle();
		arguments.putInt(AppDialog.DIALOG_ID, DELETE_GROUP_DIALOG_ID);
		arguments.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.delete_group_dialog_message, group.getName()));
		arguments.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.delete_group_dialog_positive_choice);
		arguments.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.delete_group_dialog_negative_choice);
		arguments.putSerializable(GROUP, group);
		dialog.setArguments(arguments);
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null) {
			dialog.show(fragmentManager, null);
		}
	}

	@Override
	public void OnGroupClick(Group group, long count) {
		if (count == 0) {
			Toast.makeText(getContext(), getString(R.string.group_do_not_contain_videos, group.getName()),
						   Toast.LENGTH_LONG).show();
			return;
		}
		Bundle arguments = new Bundle();
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			swapRecyclerViewsVisibility();
		}
		arguments.putString(SELECTION_PARAM, VideosContract.Columns.GROUP_ID + " = ?");
		arguments.putStringArray(SELECTION_ARGS_PARAM, new String[]{String.valueOf(group.getId())});
		LoaderManager.getInstance(this).restartLoader(VIDEOS_LOADER_ID, arguments, this);
	}

	private void deleteGroup(long groupId) {
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		ContentResolver contentResolver = activity.getContentResolver();
		contentResolver.delete(GroupsContract.buildGroupUri(groupId), null, null);
	}

	@Override
	public void onDeleteSavedVideoClick(@NonNull Video video) {
		FragmentActivity activity = getActivity();
		if (activity == null) return;
		RecyclerView.Adapter adapter = videosRecyclerView.getAdapter();
		if (orientation == Configuration.ORIENTATION_PORTRAIT && adapter != null && adapter.getItemCount() == 1) {
			swapRecyclerViewsVisibility();
		}
		ContentResolver contentResolver = activity.getContentResolver();
		String selection = VideosContract.Columns.GROUP_ID + " = " + video.getGroupId();
		contentResolver.delete(VideosContract.buildVideoUri(video.getId()), selection, null);

	}

	@Override
	public void onSavedVideoClick(Video video) {
		String url = video.getUrl();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getContext(), "No browser application found", Toast.LENGTH_LONG).show();
		}
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
		Context context = getContext();
		if (context == null) {
			throw new IllegalStateException("Context is null");
		}
		String[] projection;

		switch (id) {
			case GROUPS_VIDEO_COUNT_LOADER_ID:
				projection = new String[]
						{GroupsVideoCountContract.Columns._ID, GroupsVideoCountContract.Columns.NAME,
						 GroupsVideoCountContract.Columns.DELETABLE, GroupsVideoCountContract.Columns.VIDEO_COUNT};
				return new CursorLoader(context, GroupsVideoCountContract.CONTENT_URI, projection, null, null, null);
			case VIDEOS_LOADER_ID:
				String selection = "_id = 0";
				String[] selectionArgs = null;
				String sortOrder = null;
				if (args != null) {
					selection = args.getString(SELECTION_PARAM);
					selectionArgs = args.getStringArray(SELECTION_ARGS_PARAM);
					sortOrder = args.getString(SORT_ORDER_PARAM);
				}
				projection = new String[]
						{VideosContract.Columns._ID, VideosContract.Columns.GROUP_ID,
						 VideosContract.Columns.CHANNEL_NAME, VideosContract.Columns.GAME_NAME,
						 VideosContract.Columns.TITLE, VideosContract.Columns.VIEWS,
						 VideosContract.Columns.DURATION, VideosContract.Columns.TYPE,
						 VideosContract.Columns.CREATED_AT, VideosContract.Columns.THUMBNAIL_URL,
						 VideosContract.Columns.URL};
				return new CursorLoader(context, VideosContract.CONTENT_URI, projection, selection, selectionArgs,
										sortOrder);
			default:
				throw new IllegalArgumentException(TAG + ".onCreateLoader called with invalid loader id " + id);
		}
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case GROUPS_VIDEO_COUNT_LOADER_ID:
				groupsAdapter.swapCursor(data);
				break;
			case VIDEOS_LOADER_ID:
				videosAdapter.swapCursor(data);
				break;
		}
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		switch (loader.getId()) {
			case GROUPS_VIDEO_COUNT_LOADER_ID:
				groupsAdapter.swapCursor(null);
				break;
			case VIDEOS_LOADER_ID:
				videosAdapter.swapCursor(null);
				break;
		}
	}

	public void onPositiveDialogResult(int dialogId, Bundle args) {
		Group group = (Group) args.getSerializable(GROUP);
		if (group == null && dialogId != ADD_GROUP_DIALOG_ID) {
			throw new IllegalArgumentException("Group is not presented in bundle");
		}
		FragmentActivity activity = getActivity();
		if (activity == null) return;
		ContentResolver contentResolver = activity.getContentResolver();
		switch (dialogId) {
			case DELETE_GROUP_DIALOG_ID: {
				deleteGroup(group.getId());
				break;
			}
			case EDIT_GROUP_NAME_DIALOG_ID: {
				String enteredText = args.getString(AppDialog.DIALOG_ENTERED_TEXT);
				if (enteredText == null) {
					throw new IllegalArgumentException("Arguments must contain new group name");
				}
				String newGroupsName = enteredText.trim();
				if (group.getName().equals(newGroupsName)) {
					break;
				}
				if ("".equals(newGroupsName)) {
					Toast.makeText(activity, R.string.group_name_not_entered_message, Toast.LENGTH_LONG).show();
					break;
				}
				String[] projection = new String[]{GroupsContract.Columns._ID};
				String selection = GroupsContract.Columns.NAME + " = '" + newGroupsName + "'";
				Cursor query = contentResolver.query(GroupsContract.CONTENT_URI, projection, selection, null, null);
				if (query != null && query.getCount() > 0) {
					Toast.makeText(activity, activity.getString(R.string.group_with_entered_name_already_exist_message,
																newGroupsName), Toast.LENGTH_LONG).show();
					break;
				}
				((MainActivity) activity).hideKeyboard();
				ContentValues values = new ContentValues();
				values.put(GroupsContract.Columns.NAME, newGroupsName);
				contentResolver.update(GroupsContract.buildGroupUri(group.getId()), values, null, null);
				break;
			}
			case ADD_GROUP_DIALOG_ID: {
				String enteredText = args.getString(AppDialog.DIALOG_ENTERED_TEXT);
				if (enteredText == null) {
					throw new IllegalArgumentException("Arguments must contain new group name");
				}
				String newGroupName = enteredText.trim();
				if ("".equals(newGroupName)) {
					Toast.makeText(activity, R.string.group_name_not_entered_message, Toast.LENGTH_LONG).show();
					break;
				}
				String[] projection = new String[]{GroupsContract.Columns._ID};
				String selection = GroupsContract.Columns.NAME + " = '" + newGroupName + "'";
				Cursor query = contentResolver.query(GroupsContract.CONTENT_URI, projection, selection, null, null);
				if (query != null && query.getCount() > 0) {
					Toast.makeText(activity, activity.getString(R.string.group_with_entered_name_already_exist_message,
																newGroupName), Toast.LENGTH_LONG).show();
					break;
				}
				ContentValues values = new ContentValues();
				values.put(GroupsContract.Columns.NAME, newGroupName);
				values.put(GroupsContract.Columns.DELETABLE, Group.USERS_GROUPS_DELETABLE);
				contentResolver.insert(GroupsContract.CONTENT_URI, values);
				break;
			}
			default:
				throw new IllegalArgumentException("dialogId " + dialogId + " is invalid");
		}
	}

	public void onNegativeDialogResult(int dialogId, Bundle args) {
		// DO NOTHING
	}

	public void onDialogCancelled(int dialogId) {
		// DO NOTHING
	}

	public boolean isNeedBackButtonToReturnToGroups() {
		return orientation == Configuration.ORIENTATION_PORTRAIT && videosRecyclerView.getVisibility() == View.VISIBLE;
	}

}
