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
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sawyer.advadapters.app.R;

import butterknife.ButterKnife;

/**
 * Customized dialog fragment that handles styling the dialog to a specific look. All dialog
 * fragments should subclass this.
 */
abstract class CustomDialogFragment extends DialogFragment {
	public CustomDialogFragment() {
		setStyle(STYLE_NORMAL, R.style.AppTheme_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		setTitleBarColor();    //Done outside of onCreateDialog to ensure dialog's setContentView occurred
		return null;
	}

	/**
	 * Will apply a custom color to a dialog's title bar divider. Yes this is quite hackish and may
	 * not work on all devices. However its crash safe. Just make sure the dialog has already set
	 * it's content view.
	 */
	private void setTitleBarColor() {
		Dialog dialog = getDialog();
		if (dialog == null) return;

		View v = ButterKnife.findById(dialog, android.R.id.title);
		if (v != null && v.getParent() instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v.getParent();
			for (int index = 0; index < vg.getChildCount(); ++index) {
				View child = vg.getChildAt(index);
				if (child.getClass().equals(View.class)) {
					child.setBackgroundColor(
							child.getContext().getResources().getColor(R.color.purple_medium));
					break;
				}
			}
		}
	}
}
