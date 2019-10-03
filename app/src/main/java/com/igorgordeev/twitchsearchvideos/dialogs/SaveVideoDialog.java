package com.igorgordeev.twitchsearchvideos.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.igorgordeev.twitchsearchvideos.R;
import com.igorgordeev.twitchsearchvideos.model.Group;
import com.igorgordeev.twitchsearchvideos.model.Video;

public class SaveVideoDialog extends AppCompatDialogFragment {

	public static final String DIALOG_GROUPS = "groups";
	public static final String DIALOG_VIDEO = "video";
	private SaveVideoDialogEvents dialogEvents;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		if (!(context instanceof SaveVideoDialogEvents)) {
			throw new ClassCastException(
					context.toString() + " must implements SaveVideoDialog.SaveVideoDialogEvents interface");
		}

		dialogEvents = (SaveVideoDialogEvents) context;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		dialogEvents = null;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			throw new IllegalStateException("Activity reference is null");
		}
		final Bundle arguments = getArguments();
		final Group[] groups;
		final Video video;
		if (arguments != null) {
			groups = (Group[]) arguments.getSerializable(DIALOG_GROUPS);
			video = (Video) arguments.getSerializable(DIALOG_VIDEO);
			if (groups == null || video == null) {
				throw new IllegalArgumentException("DIALOG_GROUPS and/or DIALOG_VIDEO not present in the bundle");
			}
		} else {
			throw new IllegalArgumentException("Must pass DIALOG_GROUPS and DIALOG_VIDEO in the bundle");
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppDialogTheme);
		String[] groupsNames = new String[groups.length];
		for (int i = 0; i < groups.length; i++) {
			groupsNames[i] = groups[i].getName();
		}

		int messageString = R.string.save_video_dialog_message;
		int negativeStringId = R.string.save_video_dialog_negative_choice;
		ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.item_alert_dialog, groupsNames);
		builder.setTitle(messageString)
			   .setAdapter(adapter, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   if (dialogEvents != null) {
						   dialogEvents.onItemChosenSaveVideoDialogResult(video, groups[which]);
					   }
				   }
			   })
			   .setNegativeButton(negativeStringId, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   // DO NOTHING
				   }
			   });
		AlertDialog alertDialog = builder.create();
		ListView listView = alertDialog.getListView();
		int dividerColor = ResourcesCompat.getColor(getResources(), R.color.colorDivider, null);
		listView.setDivider(new ColorDrawable(dividerColor));
		listView.setDividerHeight(2);
		return alertDialog;
	}

	public interface SaveVideoDialogEvents {
		void onItemChosenSaveVideoDialogResult(Video video, Group group);
	}
}
