/*
 * Copyright 2014 Jay Soyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sawyer.advadapters.app.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.widget.RolodexBaseAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Renders a dialog with an option to choose which setChoiceMode should be used. Implement the
 * {@link EventListener} in order to receive back dialog results.
 */
public class ChoiceModeDialogFragment extends CustomDialogFragment {
	private static final String ARG_INTENT = "Arg Intent";

	@InjectView(R.id.radio_group)
	RadioGroup mRadioGroup;

	private EventListener mEventListener;

	public static ChoiceModeDialogFragment newInstance(Intent intent) {
		ChoiceModeDialogFragment frag = new ChoiceModeDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(ARG_INTENT, intent);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		ButterKnife.reset(this);
		super.onCancel(dialog);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_choice_mode);
		dialog.setTitle(R.string.title_dialog_choice_mode);
		ButterKnife.inject(this, dialog);
		mRadioGroup.check(R.id.choice_mode_modal);
		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		ButterKnife.reset(this);
		super.onDismiss(dialog);
	}

	@OnClick(R.id.ok)
	public void onOk(View v) {
		if (mEventListener == null) return;
		RolodexBaseAdapter.ChoiceMode choiceMode;
		switch (mRadioGroup.getCheckedRadioButtonId()) {
		case R.id.choice_mode_single:
			choiceMode = RolodexBaseAdapter.ChoiceMode.SINGLE;
			break;
		case R.id.choice_mode_multiple:
			choiceMode = RolodexBaseAdapter.ChoiceMode.MULTIPLE;
			break;
		case R.id.choice_mode_modal:
			choiceMode = RolodexBaseAdapter.ChoiceMode.MULTIPLE_MODAL;
			break;
		default:
			choiceMode = RolodexBaseAdapter.ChoiceMode.NONE;
			break;
		}
		dismiss();
		Intent intent = getArguments().getParcelable(ARG_INTENT);
		mEventListener.onSelectedChoiceMode(choiceMode, intent);
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public void onSelectedChoiceMode(RolodexBaseAdapter.ChoiceMode choiceMode, Intent intent);
	}
}
