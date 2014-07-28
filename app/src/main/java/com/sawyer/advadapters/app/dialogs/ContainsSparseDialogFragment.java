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
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.Random;

/**
 * Renders a dialog with all the options possible for checking a list for contents it may contain.
 * Implement the {@link EventListener} in order to receive back dialog results.
 */
public class ContainsSparseDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIE = "State Movie";

	private EventListener mEventListener;

	private MovieItem mMovieItem;

	public static ContainsSparseDialogFragment newInstance() {
		ContainsSparseDialogFragment frag = new ContainsSparseDialogFragment();

		Bundle bundle = new Bundle();
		int index = new Random().nextInt(MovieContent.ITEM_SPARSE.size());
		bundle.putParcelable(STATE_MOVIE, MovieContent.ITEM_SPARSE.valueAt(index));
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItem = getArguments().getParcelable(STATE_MOVIE);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_contains_sparse);
		dialog.setTitle(R.string.title_dialog_contains_movies);

		Button btn = (Button) dialog.findViewById(R.id.movie_contains_id_btn);
		btn.setOnClickListener(new OnContainsIdClickListener());
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt1);
		tv.setText("- " + mMovieItem.title);

		btn = (Button) dialog.findViewById(R.id.movie_contains_item_btn);
		btn.setOnClickListener(new OnContainsItemClickListener());
		tv = (TextView) dialog.findViewById(R.id.movie_single_txt2);
		tv.setText("- " + mMovieItem.title);

		return dialog;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public void onContainsIdClick(int barcode);

		public void onContainsItemClick(MovieItem movie);
	}

	private class OnContainsIdClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onContainsIdClick(mMovieItem.barcode());
			}
		}
	}

	private class OnContainsItemClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onContainsItemClick(mMovieItem);
			}
		}
	}
}
