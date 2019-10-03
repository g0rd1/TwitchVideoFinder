package com.igorgordeev.twitchsearchvideos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.igorgordeev.twitchsearchvideos.dialogs.AppDialog;
import com.igorgordeev.twitchsearchvideos.dialogs.SaveVideoDialog;
import com.igorgordeev.twitchsearchvideos.fragments.GroupsFragment;
import com.igorgordeev.twitchsearchvideos.fragments.SearchedVideosFragment;
import com.igorgordeev.twitchsearchvideos.model.Group;
import com.igorgordeev.twitchsearchvideos.model.Video;

public class MainActivity extends AppCompatActivity implements SaveVideoDialog.SaveVideoDialogEvents,
															   AppDialog.DialogEvents {

	private static final String CURRENT_FRAGMENT = "currentFragment";
	private AppsFragment currentFragment;
	private SearchedVideosFragment searchFragment;
	private GroupsFragment groupsFragment;
	private MenuItem searchParameters;
	private MenuItem addGroup;

	private int displayWidth;

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.navigation_search:
					swapToFragment(AppsFragment.SEARCH);
					searchParameters.setVisible(true);
					addGroup.setVisible(false);
					return true;
				case R.id.navigation_groups:
					swapToFragment(AppsFragment.GROUPS);
					searchParameters.setVisible(false);
					addGroup.setVisible(true);
					return true;
			}
			return false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		searchParameters = menu.findItem(R.id.main_menu_search_parameters);
		addGroup = menu.findItem(R.id.main_menu_add_group);
		addGroup.setVisible(false);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch (currentFragment) {
			case SEARCH:
				searchParameters.setVisible(true);
				addGroup.setVisible(false);
				break;
			case GROUPS:
				searchParameters.setVisible(false);
				addGroup.setVisible(true);
				break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_menu_add_group:
				groupsFragment.addNewGroup();
				break;
			case R.id.main_menu_search_parameters:
				searchFragment.swapSearchFiltersVisibility();
				break;
			case R.id.main_menu_about:
				showAboutDialog();
				break;
			case android.R.id.home:
				groupsFragment.swapRecyclerViewsVisibility();
				break;
		}
		return true;
	}

	private void showAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppDialogTheme);
		@SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_about, null);
		builder.setIcon(R.mipmap.ic_launcher)
			   .setView(view)
			   .setTitle(R.string.app_name)
			   .setPositiveButton(R.string.app_dialog_default_positive_choice, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   // DO NOTHING
				   }
			   });
		AlertDialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(false);

		}
		setContentView(R.layout.activity_main);
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		displayWidth = size.x;
		BottomNavigationView navView = findViewById(R.id.nav_view);
		navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
		FragmentManager fragmentManager = getSupportFragmentManager();
		searchFragment = (SearchedVideosFragment) fragmentManager.findFragmentById(R.id.activity_main_search_fragment);
		groupsFragment = (GroupsFragment) fragmentManager.findFragmentById(R.id.activity_main_groups_fragment);
		if (savedInstanceState != null) {
			currentFragment = (AppsFragment) savedInstanceState.getSerializable(CURRENT_FRAGMENT);
		}
		if (currentFragment == null) {
			swapToFragment(AppsFragment.SEARCH);
		} else {
			swapToFragment(currentFragment);
		}
	}

	private void swapToFragment(AppsFragment swatToFragment) {
		final View searchFragment = findViewById(R.id.activity_main_search_fragment);
		final View groupsFragment = findViewById(R.id.activity_main_groups_fragment);
		hideKeyboard();
		switch (swatToFragment) {
			case SEARCH:
				currentFragment = AppsFragment.SEARCH;
				searchFragment.animate().translationX(0f).setDuration(250L).start();
				groupsFragment.animate().translationX(displayWidth).setDuration(250L).start();
				break;
			case GROUPS:
				currentFragment = AppsFragment.GROUPS;
				searchFragment.animate().translationX(-displayWidth).setDuration(250L).start();
				groupsFragment.animate().translationX(0f).setDuration(250L).start();
				break;
		}
	}

	// Method taken from https://stackoverflow.com/a/17789187/11097080
	public void hideKeyboard() {
		Activity activity = this;
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) {
			view = new View(activity);
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(CURRENT_FRAGMENT, currentFragment);
		super.onSaveInstanceState(outState);
	}

	public void setUpButtonVisibility(boolean visibility) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(visibility);

		}
	}

	@Override
	public void onBackPressed() {
		if (currentFragment == AppsFragment.GROUPS && groupsFragment.isNeedBackButtonToReturnToGroups()) {
			groupsFragment.swapRecyclerViewsVisibility();
		} else {
			super.onBackPressed();
		}
	}

	public Fragment getFragment(AppsFragment fragment) {
		switch (fragment) {
			case GROUPS:
				return groupsFragment;
			case SEARCH:
				return searchFragment;
			default:
				throw new IllegalArgumentException("Incorrect fragment required");
		}
	}

	@Override
	public void onPositiveDialogResult(int dialogId, Bundle args) {
		groupsFragment.onPositiveDialogResult(dialogId, args);
	}

	@Override
	public void onNegativeDialogResult(int dialogId, Bundle args) {
		groupsFragment.onNegativeDialogResult(dialogId, args);
	}

	@Override
	public void onDialogCancelled(int dialogId) {
		groupsFragment.onDialogCancelled(dialogId);
	}

	@Override
	public void onItemChosenSaveVideoDialogResult(Video video, Group group) {
		searchFragment.onItemChosenSaveVideoDialogResult(video, group);
	}

	public enum AppsFragment {
		SEARCH, GROUPS
	}
}
