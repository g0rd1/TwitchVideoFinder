package com.igorgordeev.twitchsearchvideos.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.igorgordeev.twitchsearchvideos.R;

public class AppDialog extends AppCompatDialogFragment {

	public static final String DIALOG_ID = "id";
	public static final String DIALOG_MESSAGE = "message";
	public static final String DIALOG_POSITIVE_RID = "positive_rid";
	public static final String DIALOG_NEGATIVE_RID = "negative_rid";
	public static final String DIALOG_WITH_TEXTBOX = "with_textbox";
	public static final String DIALOG_ENTERED_TEXT = "entered_text";
	public static final String DIALOG_TEXTBOX_HINT = "textbox_hint";
	private DialogEvents dialogEvents;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		if (!(context instanceof DialogEvents)) {
			throw new ClassCastException(context.toString() + " must implements AppDialog.DialogEvents interface");
		}

		dialogEvents = (DialogEvents) context;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		dialogEvents = null;
	}

	@Override
	public void onCancel(@NonNull DialogInterface dialog) {
		if (dialogEvents != null) {
			Bundle arguments = getArguments();
			if (arguments != null) {
				int dialogId = arguments.getInt(DIALOG_ID);
				dialogEvents.onDialogCancelled(dialogId);
			}
		}
		super.onCancel(dialog);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			throw new IllegalStateException("Activity reference is null");
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppDialogTheme);

		final Bundle arguments = getArguments();
		final int dialogId;
		String messageString;
		int positiveStringId;
		int negativeStringId;
		final boolean withTextbox;
		String hint;

		if (arguments != null) {
			dialogId = arguments.getInt(DIALOG_ID);
			messageString = arguments.getString(DIALOG_MESSAGE);
			if (dialogId == 0 || messageString == null) {
				throw new IllegalArgumentException("DIALOG_ID and/or DIALOG_MESSAGE not present in the bundle");
			}

			positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID);
			if (positiveStringId == 0) {
				positiveStringId = R.string.app_dialog_default_positive_choice;
			}
			negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID);
			if (negativeStringId == 0) {
				negativeStringId = R.string.save_video_dialog_negative_choice;
			}
			withTextbox = arguments.getBoolean(DIALOG_WITH_TEXTBOX, false);
			hint = arguments.getString(DIALOG_TEXTBOX_HINT);
		} else {
			throw new IllegalArgumentException("Must pass DIALOG_ID and DIALOG_MESSAGE in the bundle");
		}
		LayoutInflater inflater = activity.getLayoutInflater();
		@SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_app, null);
		final EditText textbox = view.findViewById(R.id.appdialog_textbox);
		if (withTextbox) {
			if (hint != null) {
				textbox.setHint(hint);
			}
			builder.setView(view);
		}
		builder.setMessage(messageString)
			   .setPositiveButton(positiveStringId, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   if (dialogEvents != null) {
						   if (withTextbox) {
							   arguments.putString(DIALOG_ENTERED_TEXT, textbox.getText().toString());
						   }
						   dialogEvents.onPositiveDialogResult(dialogId, arguments);
					   }
				   }
			   })
			   .setNegativeButton(negativeStringId, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   if (dialogEvents != null) {
						   dialogEvents.onNegativeDialogResult(dialogId, arguments);
					   }
				   }
			   });

		return builder.create();
	}

	public interface DialogEvents {
		void onPositiveDialogResult(int dialogId, Bundle args);

		void onNegativeDialogResult(int dialogId, Bundle args);

		void onDialogCancelled(int dialogId);
	}
}
