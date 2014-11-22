/**
 * Copyright 2014 Jay Soyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import com.sawyer.advadapters.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SortDialogFragment extends CustomDialogFragment {
	private EventListener mEventListener;

	@InjectView(R.id.auto_sort_btn)
	Button mAutoSortBtn;
	private boolean mIsAutoSortEnabled;

	public static SortDialogFragment newInstance() {
		SortDialogFragment frag = new SortDialogFragment();
		return frag;
	}

	@OnClick(R.id.auto_sort_btn)
	public void onToggleAutoSort(View v) {
		if (mEventListener != null) {
			mEventListener.onEnableAutoSort(!mIsAutoSortEnabled);
		}
		dismiss();
	}

	@OnClick(R.id.children_sort_btn)
	public void onSortChildren(View v) {
		if (mEventListener != null) {
			mEventListener.onSortChildren();
		}
		dismiss();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_sort);
		dialog.setTitle(R.string.title_dialog_sort_movies);
		ButterKnife.inject(this, dialog);
		mAutoSortBtn.setText(
				mIsAutoSortEnabled ? R.string.btn_sort_disable_auto_sort : R.string.btn_sort_enable_auto_sort);
		return dialog;
	}

	public void setAutoSortEnabled(boolean isEnabled) {
		mIsAutoSortEnabled = isEnabled;
		if (mAutoSortBtn != null) {
			mAutoSortBtn.setText(
					mIsAutoSortEnabled ? R.string.btn_sort_disable_auto_sort : R.string.btn_sort_enable_auto_sort);
		}
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public void onEnableAutoSort(boolean isEnabled);

		public void onSortChildren();
	}
}
