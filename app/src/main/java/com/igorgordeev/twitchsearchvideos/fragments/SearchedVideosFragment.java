package com.igorgordeev.twitchsearchvideos.fragments;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igorgordeev.twitchsearchvideos.MainActivity;
import com.igorgordeev.twitchsearchvideos.R;
import com.igorgordeev.twitchsearchvideos.animations.ResizeAnimation;
import com.igorgordeev.twitchsearchvideos.contracts.GroupsContract;
import com.igorgordeev.twitchsearchvideos.contracts.GroupsVideoCountContract;
import com.igorgordeev.twitchsearchvideos.contracts.VideosContract;
import com.igorgordeev.twitchsearchvideos.data_getters.GameGetter;
import com.igorgordeev.twitchsearchvideos.data_getters.RawDataDownloader;
import com.igorgordeev.twitchsearchvideos.data_getters.VideosGetter;
import com.igorgordeev.twitchsearchvideos.dialogs.SaveVideoDialog;
import com.igorgordeev.twitchsearchvideos.model.Game;
import com.igorgordeev.twitchsearchvideos.model.Group;
import com.igorgordeev.twitchsearchvideos.model.Video;
import com.igorgordeev.twitchsearchvideos.recycler_view_adapters.SearchedVideosRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchedVideosFragment extends Fragment implements SearchedVideosRecyclerViewAdapter.OnSearchedVideosClickListener,
																VideosGetter.OnVideosDataAvailable,
																GameGetter.OnGameIdAvailable {

	// If set this constant to 0 animation would work incorrect
	private static final int MIN_VIEW_HEIGHT = 1;
	private static final int ANIMATION_DURATION_MILLIS = 500;
	private static final String TAG = "SearchedVideosFragment";
	private static boolean isFiltersVisible = true;
	private int filtersHeight;
	private SearchedVideosRecyclerViewAdapter recyclerViewAdapter;
	private CardView filters;
	private ProgressBar progressBar;
	private TextView message;
	private EditText gameName;
	private Spinner type;
	private Spinner period;
	private Spinner language;
	private Spinner sortOrder;

	public SearchedVideosFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_searched_videos, container, false);
		view.findViewById(R.id.filters_game_name_plain_text).clearFocus();
		bindViews(view);
		configureVideosRecyclerViewAdapter(view);
		setSearchButtonOnClickListener();
		setMessageOnClickListener();
		setMinimizeOnClickListener();
		setAdaptersToSpinners();
		return view;
	}

	private void bindViews(View view) {
		filters = view.findViewById(R.id.searched_videos_fragment_filters_cardview);
		progressBar = view.findViewById(R.id.searched_videos_fragment_progressBar);
		message = view.findViewById(R.id.searched_videos_fragment_message);
		gameName = filters.findViewById(R.id.filters_game_name_plain_text);
		type = filters.findViewById(R.id.filters_type_spinner);
		period = filters.findViewById(R.id.filters_period_spinner);
		language = filters.findViewById(R.id.filters_language_spinner);
		sortOrder = filters.findViewById(R.id.filters_sort_order_spinner);
	}

	private void configureVideosRecyclerViewAdapter(View view) {
		RecyclerView videos = view.findViewById(R.id.searched_videos_fragment_list);
		videos.setLayoutManager(new LinearLayoutManager(getContext()));
		setFiltersOnViewCreatedRunnable();
		if (recyclerViewAdapter == null) {
			SearchedVideosFragment listener = this;
			recyclerViewAdapter = new SearchedVideosRecyclerViewAdapter(new ArrayList<Video>(), listener);
		}
		videos.setAdapter(recyclerViewAdapter);
	}

	private void setSearchButtonOnClickListener() {
		final Button search = filters.findViewById(R.id.filters_search);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Filters onClickSearchButton: starts");
				message.setVisibility(View.GONE);
				String gameNameValue = gameName.getText().toString().trim();
				if (gameNameValue.equals("")) {
					Toast.makeText(getContext(), R.string.game_name_not_entered_message, Toast.LENGTH_LONG)
						 .show();
					return;
				}
				MainActivity activity = (MainActivity) getActivity();
				if (activity != null) {
					activity.hideKeyboard();
				}
				filters.clearFocus();
				progressBar.setVisibility(View.VISIBLE);
				recyclerViewAdapter.swapVideos(null);
				new GameGetter(SearchedVideosFragment.this).execute(gameNameValue, getString(R.string.client_id));
			}
		});
	}

	private void setMessageOnClickListener() {
		message.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				message.setVisibility(View.GONE);
			}
		});
	}

	private void setMinimizeOnClickListener() {
		ImageView minimize = filters.findViewById(R.id.filters_minimize);
		minimize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				if (activity != null) {
					activity.hideKeyboard();
				}
				filters.clearFocus();
				swapSearchFiltersVisibility();
			}
		});
	}

	private void setAdaptersToSpinners() {
		Context context = getContext();
		if (context != null) {
			Resources resources = getResources();
			ArrayAdapter<String> typeAdapter =
					new ArrayAdapter<>(context, R.layout.item_spinner,
									   resources.getStringArray(R.array.type));
			ArrayAdapter<String> periodAdapter =
					new ArrayAdapter<>(context, R.layout.item_spinner,
									   resources.getStringArray(R.array.period));
			ArrayAdapter<String> languageAdapter =
					new ArrayAdapter<>(context, R.layout.item_spinner,
									   resources.getStringArray(R.array.language));
			ArrayAdapter<String> sortOrderAdapter =
					new ArrayAdapter<>(context, R.layout.item_spinner,
									   resources.getStringArray(R.array.sort_order));
			typeAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
			periodAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
			languageAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
			sortOrderAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
			type.setAdapter(typeAdapter);
			period.setAdapter(periodAdapter);
			language.setAdapter(languageAdapter);
			sortOrder.setAdapter(sortOrderAdapter);
		}
	}

	private void setFiltersOnViewCreatedRunnable() {
		// This method start Runnable after filters is built
		// (call filters.getHeight() in onViewCreated() method returns 0)
		filters.post(new Runnable() {
			@Override
			public void run() {
				filtersHeight = filters.getHeight();
				if (!isFiltersVisible) {
					filters.getLayoutParams().height = MIN_VIEW_HEIGHT;
					filters.requestLayout();
				}
			}
		});
	}

	public void swapSearchFiltersVisibility() {
		if (isFiltersVisible) {
			ResizeAnimation resizeAnimation = new ResizeAnimation(filters, MIN_VIEW_HEIGHT, filters.getHeight());
			resizeAnimation.setDuration(ANIMATION_DURATION_MILLIS);
			filters.startAnimation(resizeAnimation);
			isFiltersVisible = false;
		} else {
			ResizeAnimation resizeAnimation = new ResizeAnimation(filters, filtersHeight, filters.getHeight());
			resizeAnimation.setDuration(ANIMATION_DURATION_MILLIS);
			filters.startAnimation(resizeAnimation);
			isFiltersVisible = true;
		}
	}

	@Override
	public void onSaveVideoClick(Video video) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		ContentResolver contentResolver = activity.getContentResolver();
		Uri uri = GroupsContract.CONTENT_URI;
		String[] projection = {GroupsContract.Columns._ID, GroupsContract.Columns.NAME,
							   GroupsContract.Columns.DELETABLE};
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		Cursor query = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
		if (query == null) {
			return;
		}
		Group[] groups = new Group[query.getCount()];
		int counter = 0;
		while (query.moveToNext()) {
			long id = query.getLong(query.getColumnIndex(GroupsContract.Columns._ID));
			String name = query.getString(query.getColumnIndex(GroupsContract.Columns.NAME));
			boolean deletable = query.getInt(query.getColumnIndex(GroupsVideoCountContract.Columns.DELETABLE)) == 1;
			groups[counter] = new Group(id, name, deletable);
			counter++;
		}
		SaveVideoDialog dialog = new SaveVideoDialog();
		Bundle arguments = new Bundle();
		arguments.putSerializable(SaveVideoDialog.DIALOG_VIDEO, video);
		arguments.putSerializable(SaveVideoDialog.DIALOG_GROUPS, groups);
		dialog.setArguments(arguments);
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null) {
			dialog.show(fragmentManager, null);
		}
	}

	@Override
	public void onVideoClick(Video video) {
		String url = video.getUrl();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getContext(), "No browser application found", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onVideosDataAvailable(List<Video> data, RawDataDownloader.DownloadStatus downloadStatus) {
		Log.d(TAG, "onVideosDataAvailable: data size is " + data.size());
		progressBar.setVisibility(View.GONE);
		if (downloadStatus == RawDataDownloader.DownloadStatus.OK) {
			if (!data.isEmpty()) {
				recyclerViewAdapter.swapVideos(data);
			} else {
				message.setVisibility(View.VISIBLE);
				message.setText(R.string.videos_not_found_message);
			}
		} else {
			message.setVisibility(View.VISIBLE);
			message.setText(R.string.twitch_connection_problem_message);
		}
	}

	public void onItemChosenSaveVideoDialogResult(Video video, Group group) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		ContentResolver contentResolver = activity.getContentResolver();
		String[] projection = new String[]{VideosContract.Columns.GROUP_ID};
		String selection = VideosContract.Columns.GROUP_ID + " = " + group.getId();
		Cursor query = contentResolver.query(VideosContract.buildVideoUri(video.getId()), projection,
											 selection, null, null);
		if (query != null && query.getCount() > 0) {
			Toast.makeText(activity, getString(R.string.group_already_have_this_video_message, group.getName()),
						   Toast.LENGTH_LONG).show();
			return;
		}
		ContentValues values = new ContentValues();
		values.put(VideosContract.Columns.GROUP_ID, group.getId());
		values.put(VideosContract.Columns._ID, video.getId());
		values.put(VideosContract.Columns.CHANNEL_NAME, video.getChannelName());
		values.put(VideosContract.Columns.GAME_NAME, video.getGameName());
		values.put(VideosContract.Columns.TITLE, video.getTitle());
		values.put(VideosContract.Columns.VIEWS, video.getViews());
		values.put(VideosContract.Columns.DURATION, video.getDuration());
		values.put(VideosContract.Columns.TYPE, video.getType());
		values.put(VideosContract.Columns.CREATED_AT, video.getCreatedAt());
		values.put(VideosContract.Columns.THUMBNAIL_URL, video.getThumbnailUrl());
		values.put(VideosContract.Columns.URL, video.getUrl());

		contentResolver.insert(VideosContract.CONTENT_URI, values);
	}

	@Override
	public void onGameIdAvailable(Game game, RawDataDownloader.DownloadStatus downloadStatus) {
		if (downloadStatus == RawDataDownloader.DownloadStatus.OK) {
			if (game != null) {
				new VideosGetter(this).execute(buildVideosGetterArguments(game));
				swapSearchFiltersVisibility();
			} else {
				message.setVisibility(View.VISIBLE);
				message.setText(R.string.game_not_found_message);
				progressBar.setVisibility(View.GONE);
			}
		} else {
			message.setVisibility(View.VISIBLE);
			message.setText(R.string.twitch_connection_problem_message);
			progressBar.setVisibility(View.GONE);
		}
	}

	private Bundle buildVideosGetterArguments(Game game) {
		Bundle arguments = new Bundle();
		Resources resources = getResources();
		String typeParameter = resources.getStringArray(
				R.array.type_parameter)[type.getSelectedItemPosition()];
		String periodParameter = resources.getStringArray(
				R.array.period_parameter)[period.getSelectedItemPosition()];
		String languageParameter = resources.getStringArray(
				R.array.language_parameter)[language.getSelectedItemPosition()];
		String sortOrderParameter = resources.getStringArray(
				R.array.sort_order_parameter)[sortOrder.getSelectedItemPosition()];
		arguments.putLong(VideosGetter.GAME_ID, game.getGameId());
		arguments.putString(VideosGetter.GAME_NAME, game.getGameName());
		arguments.putString(VideosGetter.TYPE, typeParameter);
		arguments.putString(VideosGetter.PERIOD, periodParameter);
		arguments.putString(VideosGetter.LANGUAGE, languageParameter);
		arguments.putString(VideosGetter.SORT_ORDER, sortOrderParameter);
		arguments.putString(RawDataDownloader.CLIENT_ID, getString(R.string.client_id));
		return arguments;
	}
}
