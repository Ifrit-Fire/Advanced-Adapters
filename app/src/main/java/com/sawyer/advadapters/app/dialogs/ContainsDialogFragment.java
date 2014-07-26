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
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.Arrays;
import java.util.List;

/**
 * Renders a dialog with all the options possible for checking a list for contents it may contain.
 * Implement the {@link EventListener} in order to receive back dialog results.
 */
public class ContainsDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIE_1 = "State Movie 1";
	private static final String STATE_MOVIE_2 = "State Movie 2";

	private EventListener mEventListener;

	private MovieItem[] mMovieItems;

	public static ContainsDialogFragment newInstance(MovieItem movie1, MovieItem movie2) {
		ContainsDialogFragment frag = new ContainsDialogFragment();

		Bundle bundle = new Bundle();
		bundle.putParcelable(STATE_MOVIE_1, movie1);
		bundle.putParcelable(STATE_MOVIE_2, movie2);
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItems = new MovieItem[2];
		mMovieItems[0] = getArguments().getParcelable(STATE_MOVIE_1);
		mMovieItems[1] = getArguments().getParcelable(STATE_MOVIE_2);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_contains_array);
		dialog.setTitle(R.string.title_dialog_contains_movies);

		Button btn = (Button) dialog.findViewById(R.id.movie_single_btn);
		btn.setOnClickListener(new OnContainsSingleClickListener());
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt);
		tv.setText("- " + mMovieItems[0].title);

		int containsVisibility = View.VISIBLE;
		if (mEventListener != null && !mEventListener.isContainsAllEnabled()) {
			containsVisibility = View.GONE;
		}
		btn = (Button) dialog.findViewById(R.id.movies_collection_btn);
		btn.setOnClickListener(new OnContainsCollectionClickListener());
		btn.setVisibility(containsVisibility);
		tv = (TextView) dialog.findViewById(R.id.movie_collection1_txt);
		tv.setText("- " + mMovieItems[0].title);
		((View) tv.getParent()).setVisibility(containsVisibility);
		tv = (TextView) dialog.findViewById(R.id.movie_collection2_txt);
		tv.setText("- " + mMovieItems[1].title);

		return dialog;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public boolean isContainsAllEnabled();

		public void onContainsMovieClick(MovieItem movie);

		public void onContainsMovieCollectionClick(List<MovieItem> movies);
	}

	private class OnContainsCollectionClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onContainsMovieCollectionClick(Arrays.asList(mMovieItems));
			}
		}
	}

	private class OnContainsSingleClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onContainsMovieClick(mMovieItems[0]);
			}
		}
	}
}
