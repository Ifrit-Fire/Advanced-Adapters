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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;

/**
 * Renders a dialog for displaying information to the user. Pretty much an AlertDialog
 */
public class InfoDialogFragment extends CustomDialogFragment {
	private static final String STATE_TITLE = "State title";
	private static final String STATE_MESSAGE = "State message";

	private String mTitle;
	private String mMessage;

	public static InfoDialogFragment newInstance(String title, String message) {
		InfoDialogFragment frag = new InfoDialogFragment();

		Bundle bundle = new Bundle();
		bundle.putString(STATE_TITLE, title);
		bundle.putString(STATE_MESSAGE, message);
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTitle = getArguments().getString(STATE_TITLE);
		mMessage = getArguments().getString(STATE_MESSAGE);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_info);
		dialog.setTitle(mTitle);

		TextView tv = (TextView) dialog.findViewById(android.R.id.message);
		tv.setText(mMessage);
		Button btn = (Button) dialog.findViewById(android.R.id.button1);
		btn.setOnClickListener(new OnOkClickListener());
		return dialog;
	}

	private class OnOkClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			dismiss();
		}
	}
}
