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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Renders a dialog with all the options possible for checking a list for contents it may contain.
 * Implement the {@link EventListener} in order to receive back dialog results.
 */
public class ContainsArrayDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIES = "State Movies";
	private static final String STATE_ENABLE_CONTAINS_ALL = "State Enable Contains All";

	private EventListener mEventListener;

	private boolean mIsContainsAllEnabled;
	private List<MovieItem> mMovieItems;

	public static ContainsArrayDialogFragment newInstance() {
		ContainsArrayDialogFragment frag = new ContainsArrayDialogFragment();

		Set<MovieItem> movies = new HashSet<>();
		while (movies.size() != 3) {
			int index = new Random().nextInt(MovieContent.ITEM_LIST.size());
			movies.add(MovieContent.ITEM_LIST.get(index));
		}
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(STATE_MOVIES, new ArrayList<>(movies));
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItems = getArguments().getParcelableArrayList(STATE_MOVIES);
		mIsContainsAllEnabled = getArguments().getBoolean(STATE_ENABLE_CONTAINS_ALL, true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_contains_array);
		dialog.setTitle(R.string.title_dialog_contains_movies);

		Button btn = (Button) dialog.findViewById(R.id.movie_single_btn);
		btn.setOnClickListener(new OnContainsSingleClickListener());
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt);
		tv.setText("- " + mMovieItems.get(0).title);

		int visibility = mIsContainsAllEnabled ? View.VISIBLE : View.GONE;
		btn = (Button) dialog.findViewById(R.id.movies_collection_btn);
		btn.setOnClickListener(new OnContainsCollectionClickListener());
		btn.setVisibility(visibility);
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt1);
		tv.setText("- " + mMovieItems.get(1).title);
		((View) tv.getParent()).setVisibility(visibility);
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt2);
		tv.setText("- " + mMovieItems.get(2).title);

		return dialog;
	}

	public void setEnableContainsAll(boolean enable) {
		getArguments().putBoolean(STATE_ENABLE_CONTAINS_ALL, enable);
		mIsContainsAllEnabled = enable;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public void onContainsMultipleMovieClick(List<MovieItem> movies);

		public void onContainsSingleMovieClick(MovieItem movie);
	}

	private class OnContainsCollectionClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onContainsMultipleMovieClick(mMovieItems.subList(1, 2));
			}
		}
	}

	private class OnContainsSingleClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onContainsSingleMovieClick(mMovieItems.get(0));
			}
		}
	}
}
